package info.golushkov.eve.tool.akka.models

import java.time.LocalDateTime

case class Order(
                  id: Long,
                  lastUpdate: LocalDateTime,
                  isBuy: Boolean,
                  locationId: Long,
                  regionId: Long,
                  price: Double,
                  itemId: Int,
                  remain: Int,
                  total: Int)
