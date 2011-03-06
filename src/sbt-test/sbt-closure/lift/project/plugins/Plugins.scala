import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  
  val closureCompile = "com.untyped" % "sbt-closure" % "0.1"

}
