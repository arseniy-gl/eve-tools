package info.golushkov.eve.tool.akka

import java.util.concurrent.Executors

import info.golushkov.eve.tool.akka.models._
import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.pattern._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Origin`
import akka.http.scaladsl.server.Directives._
import akka.routing.BalancingPool
import akka.util.Timeout
import info.golushkov.eve.tool.akka.actors.{ApiActor, PriceReportActor}
import info.golushkov.eve.tool.akka.actors.loaders._
import info.golushkov.eve.tool.akka.actors.mongo.RegionActor.GetAllResult
import info.golushkov.eve.tool.akka.actors.mongo._
import spray.json._
import info.golushkov.eve.tool.akka.mongodb.DB
import info.golushkov.eve.tool.akka.mongodb.models._
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

object Main extends JsonSupport {

  import ContentTypes.{`text/plain(UTF-8)` => `text`, `application/json` => `json`}
  import info.golushkov.eve.tool.akka.mongodb.models.MongoConversion._

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("eve-tool")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))
    implicit val to: Timeout = Timeout(60 seconds)

    val api = system.actorOf(Props(new ApiActor()), "ApiActor")

    val marketGroupActor = system.actorOf(BalancingPool(4).props(Props(new MarketGroupActor())), "MarketGroupActor")
    val ordersActor = system.actorOf(Props(new OrdersActor()), "OrdersActor")
    val priceActor = system.actorOf(Props(new PriceActor()), "PriceActor")
    val regionActor = system.actorOf(Props(new RegionActor()), "RegionActor")
    val itemActor = system.actorOf(Props(new ItemActor(marketGroupActor)), "ItemActor")

    val marketGroupLoader = system.actorOf(Props(new MarketGroupLoader(marketGroupActor, api)), "MarketGroupLoader")
    val ordersLoader = system.actorOf(Props(new OrdersLoader(ordersActor, regionActor, api)), "OrdersLoader")
    val priceLoader = system.actorOf(Props(new PriceLoader(priceActor, api)), "PriceLoader")
    val regionLoader = system.actorOf(Props(new RegionLoader(regionActor, api)), "RegionLoader")
    val itemLoader = system.actorOf(Props(new ItemLoader(itemActor, api)), "ItemLoader")

    val priceReportActor = system.actorOf(Props(new PriceReportActor(itemActor, priceActor, ordersActor)), "PriceReportActor")

    system.scheduler.schedule(5 seconds,    3 days,  regionLoader,       RegionLoader.Update)
    system.scheduler.schedule(5 seconds,    5 days,  marketGroupLoader,  MarketGroupLoader.Update)
    system.scheduler.schedule(5 seconds,    1 days,  priceLoader,        PriceLoader.Update)
    system.scheduler.schedule(30 seconds,   7 days,  itemLoader,         ItemLoader.Update)
    system.scheduler.schedule(10 minutes,   3 hours, ordersLoader,       OrdersLoader.Update)

    val route =
      respondWithDefaultHeader(`Access-Control-Allow-Origin`.*) {
        path("openapi") {
          get {
            complete(HttpEntity(`text`, Source.fromResource("swagger.yml").mkString))
          }
        } ~ path("regions") {
          get {
            onSuccess((regionActor ? RegionActor.GetAll).map(_.asInstanceOf[GetAllResult].regions)) { res =>
              complete(HttpEntity(`json`, res.toJson.compactPrint))
            }
          }
        } ~ path("category") {
          get {
            parameter('parent_id.as[Int].?) { parentId =>
              onSuccess(
                (marketGroupActor ? MarketGroupActor.GetOnParent(parentId))
                  .map(_.asInstanceOf[List[MarketGroup]])) {
                res =>
                  complete(HttpEntity(`json`, res.toJson.compactPrint))
              }
            }
          }
        } ~ pathPrefix("prices") {
          pathEnd {
            get {
              onSuccess((priceLoader ? PriceActor.GetAll).map(_.asInstanceOf[List[Price]])) { res =>
                complete(HttpEntity(`json`, res.toJson.compactPrint))
              }
            }
          } ~ path("report") {

            get {
              onSuccess {
                (priceReportActor ? PriceReportActor.MakeReport).map(_.asInstanceOf[List[PriceReportRow]])
              } { res =>
                complete(HttpEntity(`json`, res.toJson.compactPrint))
              }
            }
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8090)

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate())
    }))

  }
}
