package info.golushkov.eve.tool.akka.actors.mongo

import java.time.LocalDateTime
import java.util.concurrent.Executors

import akka.actor.Actor
import akka.pattern.pipe
import akka.util.Timeout
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import info.golushkov.eve.tool.akka.models.Order
import info.golushkov.eve.tool.akka.mongodb.DB
import info.golushkov.eve.tool.akka.mongodb.models.OrderMongo

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class OrdersActor extends Actor {
  import OrdersActor._
  import info.golushkov.eve.tool.akka.mongodb.models.MongoConversion._
  private val coll = DB.database.getCollection[OrderMongo]("regions")


  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val to: Timeout = Timeout(5 seconds)

  override def receive = {
    case GetOnItemId(id) =>
      coll.find(equal("itemId", id)).toFuture().map(_.map(_.asScala).toList) pipeTo sender()

    case WriteOrUpdate(order) =>
      val s = sender()
      coll.find(equal("id", order.id)).toFuture().map(res => WriteOrUpdate2(res.headOption, order.asMongo)).pipeTo(self)(s)

    case WriteOrUpdate2(Some(res), order) =>
      coll.updateOne(equal("_id", res._id), combine(
        set("id", order.id),
        set("lastUpdate", order.lastUpdate),
        set("isBuy", order.isBuy),
        set("locationId", order.locationId),
        set("price", order.price),
        set("itemId", order.itemId),
        set("remain", order.remain),
        set("total", order.total)
      )).toFuture

    case WriteOrUpdate2(None, order) =>
      coll.insertOne(order).toFuture
  }

  private case class WriteOrUpdate2(res: Option[OrderMongo], order: OrderMongo)
}

object OrdersActor {
  case class GetOnItemId(id: Int)
  case class WriteOrUpdate(order: Order)
}