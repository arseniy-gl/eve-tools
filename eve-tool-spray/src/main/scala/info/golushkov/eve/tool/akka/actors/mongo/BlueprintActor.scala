package info.golushkov.eve.tool.akka.actors.mongo

import java.util.concurrent.Executors

import akka.actor.Actor
import akka.util.Timeout
import akka.pattern.pipe
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import info.golushkov.eve.tool.akka.models.Blueprint
import info.golushkov.eve.tool.akka.mongodb.DB
import info.golushkov.eve.tool.akka.mongodb.models._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

class BlueprintActor extends Actor {
  import BlueprintActor._
  import MongoConversion._
  private val coll = DB.database.getCollection[BlueprintMongo]("blueprints")

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val to: Timeout = Timeout(5 seconds)

  override def receive: PartialFunction[Any, Unit] = {
    case GetAll =>
      coll.find().observeOn(ec).toFuture().map(_.map(_.asScala)) pipeTo sender()
    case GetOnId(id) =>
      coll.find(equal("id", id)).observeOn(ec).toFuture().map(_.headOption.map(_.asScala).get) pipeTo sender()

    case WriteOrUpdate(blueprint) =>
      val exist = Await.result(coll.find(equal("id", blueprint.id)).observeOn(ec).toFuture(), to.duration).headOption
      exist.map(_._id) match {
        case Some(_id) =>
          coll.updateOne(equal("_id", _id), combine(
            set("name", blueprint.name),
            set("activities", blueprint.activities),
            set("maxProductionLimit", blueprint.maxProductionLimit)))
        case None =>
          coll.insertOne(blueprint.asMongo)
      }
  }
}
object BlueprintActor {
  case object GetAll
  case class GetOnId(id: Int)
  case class WriteOrUpdate(blueprint: Blueprint)
}
