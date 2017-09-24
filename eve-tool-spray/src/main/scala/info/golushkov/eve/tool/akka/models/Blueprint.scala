package info.golushkov.eve.tool.akka.models

case class Blueprint(id: Int, name: Option[String], activities: BlueprintActivities, maxProductionLimit: Int)
