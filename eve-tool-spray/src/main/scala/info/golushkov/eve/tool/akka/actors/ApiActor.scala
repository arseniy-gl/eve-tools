package info.golushkov.eve.tool.akka.actors

import java.time.{LocalDate, LocalDateTime}

import akka.actor.{Actor, ActorRef}
import info.golushkov.eve.tool.akka.models._
import io.swagger.client.api.{MarketApi, UniverseApi}

class ApiActor extends Actor {

  import ApiActor._

  private val universeApi = new UniverseApi()
  private val marketApi = new MarketApi()

  override def receive: PartialFunction[Any, Unit] = {
    case GetUniverseRegions =>
      sender() ! universeApi.getUniverseRegions().getOrElse(Nil)

    case GetUniverseRegionsRegionId(id) =>
      universeApi.getUniverseRegionsRegionId(id) match {
        case Some(r) =>
          sender() ! Region(
            id = r.regionId,
            name = r.name,
            constellations = r.constellations)

        case None => ()

      }

    case GetUniverseTypesTypeId(id) =>
      universeApi.getUniverseTypesTypeId(id) match {
        case Some(i) =>
          sender() ! Item(
            id = i.typeId,
            iconId = i.iconId,
            name = i.name,
            groupId = i.groupId)

        case None => ()
      }

    case GetMarketsGroups =>
      sender() ! marketApi.getMarketsGroups().getOrElse(Nil)

    case GetMarketsGroupsMarketGroupId(id) =>
      marketApi.getMarketsGroupsMarketGroupId(id) match {
        case Some(mg) =>
          sender() ! MarketGroup(
            id = mg.marketGroupId,
            name = mg.name,
            parentId = mg.parentGroupId,
            types = mg.types)
      }

    case GetMarketsRegionIdHistory(regionId, typeId) =>
      sender() ! marketApi.getMarketsRegionIdHistory(regionId, typeId)

    case GetMarketsPrices =>
      sender() ! marketApi
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

    case GetMarketsRegionIdOrders(regionId, page) =>
      val res = marketApi
        .getMarketsRegionIdOrders(regionId = regionId, page = Some(page))
        .getOrElse(Nil)
        .map { o =>
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
      val ids = universeApi
        .getUniverseTypes(page = Some(page))
        .getOrElse(Nil)
      if (ids.nonEmpty) {
        sender() ! ids
        self forward GetUniverseTypes(page + 1)
      }
  }

}

object ApiActor {

  case class GetUniverseTypes(page: Int = 1)

  case class GetMarketsRegionIdOrders(regionId: Int, page: Int = 1)

  case object GetUniverseRegions

  case class GetUniverseRegionsRegionId(id: Int)

  case class GetUniverseTypesTypeId(id: Int)

  case object GetMarketsGroups

  case class GetMarketsGroupsMarketGroupId(id: Int)

  case object GetMarketsPrices

  case class GetMarketsRegionIdHistory(regionId: Int, typeId: Int)

}
