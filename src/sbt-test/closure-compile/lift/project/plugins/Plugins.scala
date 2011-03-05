import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  
  val closureCompile = "com.untyped" % "closure-sbt-plugin" % "0.2"

}
