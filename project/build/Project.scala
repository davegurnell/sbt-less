import sbt._

class Project(info: ProjectInfo) extends PluginProject(info) with test.ScalaScripted {
  
  val closure = "com.google.javascript" % "closure-compiler" % "r606"

  override def scriptedSbt = "0.7.4"
  override def scriptedBufferLog = false

  override def testAction = testNoScripted

  lazy val default = scripted dependsOn(publishLocal) describedAs("Publishes locally and tests against example projects")
   
  // val publishTo = Resolver.file("local-s3-repo.coderlukes.com-mirror",
  //                               new java.io.File(Path.userHome.toString, "code/repo.coderlukes.com"))

}
