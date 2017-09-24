package info.golushkov.eve.tool.akka.models

case class MarketGroup(
                        id: Int,
                        name: String,
                        parentId: Option[Int],
                        types: List[Int])

