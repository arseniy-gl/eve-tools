package info.golushkov.eve.tool.akka.models

import java.time.LocalDate

case class Price(
                  lastUpdate: LocalDate,
                  adjustedPrice: Double,
                  averagePrice: Double,
                  typeId:Int)
