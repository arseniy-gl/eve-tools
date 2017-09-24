package info.golushkov.eve.tool.akka.models

import java.time.LocalDate

case class TradeHistory(average: Float, date:LocalDate, highest: Float, lowest: Float, orderCount: Long, volume: Long)

