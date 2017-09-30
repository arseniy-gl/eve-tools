package info.golushkov.eve.tool.akka.models

import java.time.LocalDate

case class TradeHistory(
                         regionId: Long,
                         itemId: Long,
                         average: Float,
                         date:LocalDate,
                         highest: Float,
                         lowest: Float,
                         orderCount: Long,
                         volume: Long)

