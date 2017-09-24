package info.golushkov.eve.tool.akka.actors.mongo

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import akka.pattern.pipe
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import info.golushkov.eve.tool.akka.models.MarketGroup
import info.golushkov.eve.tool.akka.mongodb.DB
import info.golushkov.eve.tool.akka.mongodb.models._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MarketGroupActor extends Actor {
  import MarketGroupActor._
  import MongoConversion._
  private val coll = DB.database.getCollection[MarketGroupMongo]("marketGroup")

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val to: Timeout = Timeout(5 seconds)

  override def receive: Actor.Receive = {
    case GetAll =>
      coll.find().toFuture().map(_.map(_.asScala).toList) to sender()

    case WriteOrUpdate(marketGroup) =>
      val s: ActorRef = sender()
      coll.find(equal("id", marketGroup.id)).toFuture().map(res => WriteOrUpdate2(res.headOption, marketGroup)).pipeTo(self)(s)

    case WriteOrUpdate2(Some(res), marketGroup) =>
      coll.updateOne(equal("_id", res._id), combine(
        set("id", marketGroup.id),
        set("name", marketGroup.name),
        set("parentId", marketGroup.parentId),
        set("types", marketGroup.types))).toFuture

    case WriteOrUpdate2(None, marketGroup) =>
      coll.insertOne(marketGroup.asMongo).toFuture
  }

  private case class WriteOrUpdate2(res: Option[MarketGroupMongo], marketGroup: MarketGroup)

}

object MarketGroupActor {
  case object GetAll
  case class WriteOrUpdate(marketGroup: MarketGroup)
}
