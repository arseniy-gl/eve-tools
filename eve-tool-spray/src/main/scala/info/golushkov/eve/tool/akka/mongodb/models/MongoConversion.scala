package info.golushkov.eve.tool.akka.mongodb.models

import spray.json._
import info.golushkov.eve.tool.akka.models._

import scala.language.implicitConversions

object MongoConversion extends MongoConversionAsMongo with MongoConversionAsScala

trait MongoConversionAsMongo extends JsonSupport {
  import info.golushkov.eve.tool.akka.utils.DateConverter._
  
  implicit def regionAsMongo(m: Region): AsMongo[RegionMongo] = {
    new AsMongo(RegionMongo(m.id, m.name, m.constellations))
  }

  implicit def marketGroupAsMongo(m: MarketGroup): AsMongo[MarketGroupMongo] = {
    new AsMongo(MarketGroupMongo(m.id, m.name, m.parentId, m.types))
  }

  implicit def blueprintAsMongo(m: Blueprint): AsMongo[BlueprintMongo] = {
    new AsMongo(BlueprintMongo(m.id, m.name, m.activities.toJson.compactPrint, m.maxProductionLimit))
  }

  implicit def tradeHistoryAsMongo(m: TradeHistory): AsMongo[TradeHistoryMongo] = {
    new AsMongo(TradeHistoryMongo(m.average, m.date.toDate, m.highest, m.lowest, m.orderCount, m.volume))
  }

  implicit def itemAsMongo(m: Item): AsMongo[ItemMongo] = {
    new AsMongo(ItemMongo(m.id,  m.iconId, m.name, m.groupId))
  }

  implicit def priceAsMongo(m: Price): AsMongo[PriceMongo] = {
    new AsMongo(PriceMongo(m.lastUpdate.toDate, m.adjustedPrice,  m.averagePrice, m.typeId))
  }

  implicit def orderAsMongo(m: Order): AsMongo[OrderMongo] = {
    new AsMongo(OrderMongo(m.id, m.lastUpdate.toDate, m.isBuy, m.locationId, m.regionId, m.price, m.itemId, m.remain, m.total))
  }

}
trait MongoConversionAsScala extends JsonSupport  {
  import info.golushkov.eve.tool.akka.utils.DateConverter._

  implicit def regionAsScala(m: RegionMongo): AsScala[Region] = {
    new AsScala(Region(m.id, m.name, m.constellations))
  }

  implicit def marketGroupAsScala(m: MarketGroupMongo): AsScala[MarketGroup] = {
    new AsScala(MarketGroup(m.id, m.name, m.parentId, m.types))
  }

  implicit def blueprintAsScala(m: BlueprintMongo): AsScala[Blueprint] = {
    new AsScala(Blueprint(m.id, m.name, m.activities.parseJson.convertTo[BlueprintActivities], m.maxProductionLimit))
  }

  implicit def tradeHistoryAsScala(m: TradeHistoryMongo): AsScala[TradeHistory] = {
    new AsScala(TradeHistory(m.average, m.date.toLocalDate, m.highest, m.lowest, m.orderCount, m.volume))
  }

  implicit def itemAsScala(m: ItemMongo): AsScala[Item] = {
    new AsScala(Item(m.id,  m.iconId, m.name, m.groupId))
  }

  implicit def priceAsScala(m: PriceMongo): AsScala[Price] = {
    new AsScala(Price(m.lastDate.toLocalDate, m.adjustedPrice,  m.averagePrice, m.typeId))
  }

  implicit def orderAsScala(m: OrderMongo): AsScala[Order] = {
    new AsScala(Order(m.id, m.lastUpdate.toLocalDateTime, m.isBuy, m.locationId, m.regionId, m.price, m.itemId, m.remain, m.total))
  }

}

class AsMongo[A](op: => A) {
  def asMongo: A = op
}

class AsScala[A](op: => A) {
  def asScala: A = op
}
