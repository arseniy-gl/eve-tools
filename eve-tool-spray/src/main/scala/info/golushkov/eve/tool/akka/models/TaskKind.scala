package info.golushkov.eve.tool.akka.models

sealed trait TaskKind
object TaskKind{
  case object LoadRegions extends TaskKind
  case object LoadMarketGroups extends TaskKind
  case object LoadBlueprints extends TaskKind
  case object LoadItems extends TaskKind
}
