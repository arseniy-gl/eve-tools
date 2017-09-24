package info.golushkov.eve.tool.akka.models

case class PriceReportRow(
                           name: String,
                           price: Double,
                           lowBuy: Double,
                           highBuy: Double,
                           lowSell: Double,
                           highSell: Double)
