import sbt._

import java.io.File

class Project(info: ProjectInfo) extends PluginProject(info) with test.ScalaScripted {
  
  // val asualRepo = "Asual Public Repository" at "http://www.asual.com/maven/content/groups/public"
  val asualRepo = "Untyped Public Repo" at "http://repo.untyped.com"
  
  val lessRhino = "com.asual.lesscss" % "lesscss-engine" % "1.1.3"

  override def scriptedSbt = "0.7.4"
  override def scriptedBufferLog = false

  override def testAction = testNoScripted
  
  lazy val default = scripted dependsOn(publishLocal) describedAs("Publishes locally and tests against example projects")
  
  val publishTo = {
    val host = System.getenv("DEFAULT_REPO_HOST")
    val path = System.getenv("DEFAULT_REPO_PATH")
    val user = System.getenv("DEFAULT_REPO_USER")
    val keyfile = new File(System.getenv("DEFAULT_REPO_KEYFILE"))
    
    Resolver.sftp("Default Repo", host, path).as(user, keyfile)
  }
  
}
