package info.golushkov.eve.tool.akka.actors.mongo

import java.util.concurrent.Executors

import akka.actor.Actor
import akka.util.Timeout
import akka.pattern.pipe
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import info.golushkov.eve.tool.akka.models.Price
import info.golushkov.eve.tool.akka.mongodb.DB
import info.golushkov.eve.tool.akka.mongodb.models._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

class PriceActor extends Actor {
  import PriceActor._
  import MongoConversion._
  private val coll = DB.database.getCollection[PriceMongo]("prices")

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val to: Timeout = Timeout(5 seconds)

  override def receive: PartialFunction[Any, Unit] = {
    case GetAll =>
      coll.find().toFuture().map(_.map(_.asScala)) pipeTo sender()
    case GetOn(id) =>
      coll.find(equal("typeId", id)).toFuture().map(_.headOption.map(_.asScala)) pipeTo sender()

    case GetCount =>
      coll.count().observeOn(ec).toFuture().map(l => CountPrice(l.sum)) pipeTo sender()

    case WriteOrUpdate(price) =>
      val exist = Await.result(coll.find(equal("typeId", price.typeId)).observeOn(ec).toFuture(), to.duration).headOption
      exist.map(_._id) match {
        case Some(_id) =>
          coll.updateOne(equal("_id", _id), combine(
            set("adjustedPrice", price.adjustedPrice),
            set("averagePrice", price.averagePrice))).toFuture()
        case None =>
            coll.insertOne(price.asMongo).toFuture()
      }
  }
}

object PriceActor {
  case object GetCount
  case class CountPrice(value: Long)
  case class GetOn(id: Int)
  case object GetAll
  case class WriteOrUpdate(p: Price)
}