package info.golushkov.eve.tool.akka.actors.mongo

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import akka.pattern.pipe
import info.golushkov.eve.tool.akka.models.TradeHistory
import info.golushkov.eve.tool.akka.mongodb.DB
import info.golushkov.eve.tool.akka.mongodb.models._
import info.golushkov.eve.tool.akka.utils.UberFuture
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TradeHistoryActor extends Actor with UberFuture with ActorLogging {
  import MongoConversion._
  import TradeHistoryActor._
  import info.golushkov.eve.tool.akka.utils.DateConverter._
  private val coll = DB.database.getCollection[TradeHistoryMongo]("history")

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val to: Timeout = Timeout(5 seconds)

  override def receive: Actor.Receive = {
    case WriteOrUpdate(h) =>
      coll
        .findOneAndReplace(
          and(
            equal("date", h.date.toDate),
            equal("regionId", h.regionId),
            equal("itemId", h.itemId)), h.asMongo)
        .toFuture()
  }

}

object TradeHistoryActor {
  case class WriteOrUpdate(history: TradeHistory)
}