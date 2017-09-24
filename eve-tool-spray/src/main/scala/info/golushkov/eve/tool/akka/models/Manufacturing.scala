package info.golushkov.eve.tool.akka.models

case class Manufacturing(materials: Option[List[ManufacturingProduct]], products: Option[List[ManufacturingProduct]], time: Long)

