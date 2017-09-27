package info.golushkov.eve.tool.akka.models

case class PriceReportRow(
                           itemName: String,
                           regionName: String,
                           bestBuy: Double,
                           bestSell: Double)
