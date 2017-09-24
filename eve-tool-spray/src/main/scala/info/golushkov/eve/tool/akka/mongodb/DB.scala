package info.golushkov.eve.tool.akka.mongodb
import com.mongodb.ConnectionString
import com.mongodb.connection.ConnectionPoolSettings
import info.golushkov.eve.tool.akka.models.{BlueprintActivities, BlueprintTime, Manufacturing, ManufacturingProduct}
import info.golushkov.eve.tool.akka.mongodb.models._
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCollection, MongoDatabase, Observable}
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.connection._

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
  private val mongoClient: MongoClient = MongoClient({
    val connectionString = new ConnectionString("mongodb://localhost:27017")
    MongoClientSettings
      .builder()
      .codecRegistry(DEFAULT_CODEC_REGISTRY)
      .clusterSettings(
        ClusterSettings
          .builder()
          .applyConnectionString(connectionString)
          .build())
      .connectionPoolSettings(
        ConnectionPoolSettings
          .builder()
          .applyConnectionString(connectionString)
          .maxWaitQueueSize(1000)
          .maxSize(200)
          .build())
      .serverSettings(
        ServerSettings
          .builder()
          .build())
      .credentialList(connectionString.getCredentialList)
      .sslSettings(
        SslSettings.builder()
          .applyConnectionString(connectionString)
          .build())
      .socketSettings(
        SocketSettings
          .builder()
          .applyConnectionString(connectionString)
          .build())
      .build()
  })
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
