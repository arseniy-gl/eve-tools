package info.golushkov.eve.tool.akka.models

case class Region(
                   id: Int, 
                   name: String, 
                   constellations: List[Int])
