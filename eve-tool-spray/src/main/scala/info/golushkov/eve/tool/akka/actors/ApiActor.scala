package info.golushkov.eve.tool.akka.actors

import java.time.{LocalDate, LocalDateTime}

import akka.actor.{Actor, ActorLogging, ActorRef}
import info.golushkov.eve.tool.akka.models._
import info.golushkov.eve.tool.akka.utils.DateConverter
import io.swagger.client.api.{MarketApi, UniverseApi}

import scala.util.control.NonFatal

class ApiActor extends Actor with ActorLogging {

  import ApiActor._
  import DateConverter._

  private val universeApi = new UniverseApi()
  private val marketApi = new MarketApi()

  override def receive: PartialFunction[Any, Unit] = {
    case GetUniverseRegions =>
      sender() ! withRepeater { universeApi.getUniverseRegions().getOrElse(Nil) }

    case GetUniverseRegionsRegionId(id) =>
      withRepeater { universeApi.getUniverseRegionsRegionId(id) } match {
        case Some(r) =>
          sender() ! Region(
            id = r.regionId,
            name = r.name,
            constellations = r.constellations.map(_.toLong))

        case None => ()

      }

    case GetUniverseTypesTypeId(id) =>
      withRepeater { universeApi.getUniverseTypesTypeId(id) } match {
        case Some(i) =>
          sender() ! Item(
            id = i.typeId,
            iconId = i.iconId,
            name = i.name,
            groupId = i.groupId)

        case None => ()
      }

    case GetMarketsGroups =>
      sender() ! withRepeater { marketApi.getMarketsGroups().getOrElse(Nil) }

    case GetMarketsGroupsMarketGroupId(id) =>
      withRepeater {
        marketApi.getMarketsGroupsMarketGroupId(id)
      } match {
        case Some(mg) =>
          sender() ! MarketGroup(
            id = mg.marketGroupId,
            name = mg.name,
            parentId = mg.parentGroupId,
            types = mg.types)
      }

    case GetMarketsRegionIdHistory(regionId, typeId) =>
      sender() ! withRepeater {
        marketApi.getMarketsRegionIdHistory(regionId.toInt, typeId)
          .getOrElse(Nil)
          .map { res =>
            TradeHistory(
              regionId = regionId,
              itemId = typeId,
              average = res.average,
              date = res.date.toLocalDate,
              highest = res.highest,
              lowest = res.lowest,
              orderCount = res.orderCount,
              volume = res.volume)
          }
      }

    case GetMarketsPrices =>
      sender() ! withRepeater {
        marketApi
          .getMarketsPrices()
          .getOrElse(Nil)
          .flatMap { res =>
            for {
              adjustedPrice <- res.adjustedPrice.map(_.doubleValue())
              averagePrice <- res.averagePrice.map(_.doubleValue())
            } yield {
              Price(
                lastUpdate = LocalDate.now(),
                adjustedPrice,
                averagePrice,
                res.typeId.intValue())
            }
          }
      }

    case GetMarketsRegionIdOrders(regionId, page) =>
      val res = withRepeater {
        marketApi
          .getMarketsRegionIdOrders(regionId = regionId.toInt, page = Some(page))
          .getOrElse(Nil)
      }.map { o =>
        Order(
          id = o.orderId,
          lastUpdate = LocalDateTime.now(),
          isBuy = o.isBuyOrder,
          locationId = o.locationId,
          price = o.price,
          itemId = o.typeId,
          remain = o.volumeRemain,
          total = o.volumeTotal
        )
      }
      if(res.nonEmpty) {
        sender() ! res
        self forward GetMarketsRegionIdOrders(regionId, page+1)
      }

    case GetUniverseTypes(page) =>
      val ids = withRepeater {
        universeApi
          .getUniverseTypes(page = Some(page))
          .getOrElse(Nil)
      }
      if (ids.nonEmpty) {
        sender() ! ids
        self forward GetUniverseTypes(page + 1)
      }
  }

  def withRepeater[T](fun: => T): T = {
    var f = true
    var result:Option[T] = None // TODO ужасный ксотыль!
    while (f) {
      try {
        result = Some(fun())
        f = false
      } catch {
        case NonFatal(ex) =>
          f = true
          log.error(ex.getMessage)
          Thread.sleep(1000)
      }
    }
    result.get
  }

}

object ApiActor {

  case class GetUniverseTypes(page: Int = 1)

  case class GetMarketsRegionIdOrders(regionId: Long, page: Int = 1)

  case object GetUniverseRegions

  case class GetUniverseRegionsRegionId(id: Int)

  case class GetUniverseTypesTypeId(id: Int)

  case object GetMarketsGroups

  case class GetMarketsGroupsMarketGroupId(id: Int)

  case object GetMarketsPrices

  case class GetMarketsRegionIdHistory(regionId: Long, typeId: Int)

}
