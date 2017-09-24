package info.golushkov.eve.tool.akka.models

case class BlueprintActivities(
                                copying: Option[BlueprintTime],
                                manufacturing: Option[Manufacturing],
                                researchMaterial: Option[BlueprintTime],
                                researchTime: Option[BlueprintTime])
