package info.golushkov.eve.tool.akka.mongodb

import java.util.Date

import info.golushkov.eve.tool.akka.models._
import org.mongodb.scala.bson.ObjectId

package object models {
  case class RegionMongo(
                          _id: ObjectId,
                          id: Long,
                          name: String,
                          constellations: List[Long])
  object RegionMongo {
    def apply(
               id: Long,
               name: String,
               constellations: List[Long]): RegionMongo = new RegionMongo(new ObjectId(), id, name, constellations)
  }

  case class MarketGroupMongo(
                               _id: ObjectId,
                               id: Int,
                               name: String,
                               parentId: Option[Int],
                               types: List[Int])
  object MarketGroupMongo {
    def apply(
               id: Int,
               name: String,
               parentId: Option[Int],
               types: List[Int]): MarketGroupMongo = new MarketGroupMongo(new ObjectId(), id, name, parentId, types)
  }

  case class BlueprintMongo(
                             _id: ObjectId,
                             id: Int,
                             name: Option[String],
                             activities: String,
                             maxProductionLimit: Int)

  object BlueprintMongo {
    def apply(
               id: Int,
               name: Option[String],
               activities: String,
               maxProductionLimit: Int): BlueprintMongo =
      new BlueprintMongo(new ObjectId(), id, name, activities, maxProductionLimit)
  }

  case class TradeHistoryMongo(
                                _id: ObjectId,
                                regionId: Long,
                                itemId: Long,
                                average: Float,
                                date: Date,
                                highest: Float,
                                lowest: Float,
                                orderCount: Long,
                                volume: Long)

  object TradeHistoryMongo {
    def apply(
               regionId: Long,
               itemId: Long,
               average: Float,
               date: Date,
               highest: Float,
               lowest: Float,
               orderCount: Long,
               volume: Long): TradeHistoryMongo = new TradeHistoryMongo(
      new ObjectId(),
      regionId,
      itemId,
      average,
      date,
      highest,
      lowest,
      orderCount,
      volume)
  }

  case class ItemMongo(_id: ObjectId, id: Int, iconId: Option[Int], name: String, groupId: Int)
  object ItemMongo {
    def apply(id: Int, iconId: Option[Int], name: String, groupId: Int):ItemMongo = new ItemMongo(new ObjectId(), id, iconId, name, groupId)
  }

  case class PriceMongo(_id: ObjectId, lastDate: Date, adjustedPrice: Double, averagePrice:Double, typeId:Int)
  object PriceMongo {
    def apply(lastDate: Date, adjustedPrice: Double, averagePrice: Double, typeId: Int): PriceMongo =
      new PriceMongo(new ObjectId(), lastDate, adjustedPrice, averagePrice, typeId)
  }

  case class OrderMongo(
                    _id: ObjectId,
                    id: Long,
                    lastUpdate: Date,
                    isBuy: Boolean,
                    locationId: Long,
                    regionId: Long,
                    price: Double,
                    itemId: Int,
                    remain: Int,
                    total: Int)
  object OrderMongo {
    def apply(
               id: Long,
               lastUpdate: Date,
               isBuy: Boolean,
               locationId: Long,
               regionId: Long,
               price: Double,
               itemId: Int,
               remain: Int,
               total: Int): OrderMongo = new OrderMongo(new ObjectId(), id, lastUpdate, isBuy, locationId, regionId,
      price, itemId, remain, total)
  }
}
