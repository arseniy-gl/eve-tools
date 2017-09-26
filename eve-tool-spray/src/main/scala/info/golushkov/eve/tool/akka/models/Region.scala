package info.golushkov.eve.tool.akka.models

case class Region(
                   id: Long,
                   name: String, 
                   constellations: List[Long])
