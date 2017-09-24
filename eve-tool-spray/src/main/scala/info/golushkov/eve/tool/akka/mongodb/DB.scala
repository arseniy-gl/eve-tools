package info.golushkov.eve.tool.akka.mongodb
import info.golushkov.eve.tool.akka.models.{BlueprintActivities, BlueprintTime, Manufacturing, ManufacturingProduct}
import info.golushkov.eve.tool.akka.mongodb.models._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase, Observable}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

object DB {
  private val codecRegistry = fromRegistries(
    fromProviders(
      classOf[RegionMongo],
      classOf[MarketGroupMongo],
      classOf[BlueprintMongo],
      classOf[BlueprintActivities],
      classOf[Manufacturing],
      classOf[ManufacturingProduct],
      classOf[BlueprintTime],
      classOf[ItemMongo],
      classOf[PriceMongo],
      classOf[OrderMongo],
      classOf[TradeHistoryMongo]),
    DEFAULT_CODEC_REGISTRY)
  private val collections = "regions"::Nil
  private val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("eve-tool").withCodecRegistry(codecRegistry)

  checkCollection()

  private def checkCollection(): Unit = {
    val names = scala.collection.mutable.HashSet[String]()
    database.listCollectionNames().subscribe(
      (name: String) =>{ names += name; println(name) },
      (e: Throwable) => {},
      () => {
        collections.filterNot(names).foreach(database.createCollection)
      })
  }

}
