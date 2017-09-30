package info.golushkov.eve.tool.akka.models

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object DateFormat extends RootJsonFormat[LocalDate] {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    def write(obj: LocalDate): JsValue = {
      JsString(formatter.format(obj))
    }

    def read(json: JsValue): LocalDate = json match {
      case JsString(s) => LocalDate.from(formatter.parse(s))
      case _ => LocalDate.now()
    }
  }

  implicit object DateTimeFormat extends RootJsonFormat[LocalDateTime] {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    def write(obj: LocalDateTime): JsValue = {
      JsString(formatter.format(obj))
    }

    def read(json: JsValue): LocalDateTime = json match {
      case JsString(s) => LocalDateTime.from(formatter.parse(s))
      case _ => LocalDateTime.now()
    }
  }

  implicit val regionFormat: RootJsonFormat[Region] = jsonFormat3(Region)

  implicit val MarketGroupFormat: RootJsonFormat[MarketGroup] = jsonFormat4(MarketGroup)
  implicit val BlueprintTimeFormat: RootJsonFormat[BlueprintTime] = jsonFormat1(BlueprintTime)
  implicit val ManufacturingProductFormat: RootJsonFormat[ManufacturingProduct] = jsonFormat2(ManufacturingProduct)
  implicit val ManufacturingFormat: RootJsonFormat[Manufacturing] = jsonFormat3(Manufacturing)
  implicit val BlueprintActivitiesFormat: RootJsonFormat[BlueprintActivities] = jsonFormat4(BlueprintActivities)
  implicit val BlueprintFormat: RootJsonFormat[Blueprint] = jsonFormat4(Blueprint)

  implicit val TradeHistoryFormat: RootJsonFormat[TradeHistory] = jsonFormat8(TradeHistory)
  implicit val ItemFormat: RootJsonFormat[Item] = jsonFormat4(Item)
  implicit val PriceFormat: RootJsonFormat[Price] = jsonFormat4(Price)
  implicit val OrderFormat: RootJsonFormat[Order] = jsonFormat9(Order)
  implicit val PriceReportRowFormat: RootJsonFormat[PriceReportRow] = jsonFormat4(PriceReportRow)

}
