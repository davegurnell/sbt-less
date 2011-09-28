name := "sbt-less"

version := "0.2-SNAPSHOT"

organization := "untyped"

scalaVersion := "2.9.1"

sbtPlugin := true

resolvers += "Untyped Public Repo" at "http://repo.untyped.com"

libraryDependencies += "com.asual.lesscss" % "lesscss-engine" % "1.1.3"

seq(ScriptedPlugin.scriptedSettings: _*)

// Make the scripted SBT plugin print things immediately.
// Useful when inserting pause statements into the test scripts:
scriptedBufferLog := false

// lazy val default = scripted dependsOn(publishLocal) describedAs("Publishes locally and tests against example projects")
// 
// val publishTo = {
//   val host = System.getenv("DEFAULT_REPO_HOST")
//   val path = System.getenv("DEFAULT_REPO_PATH")
//   val user = System.getenv("DEFAULT_REPO_USER")
//   val keyfile = new File(System.getenv("DEFAULT_REPO_KEYFILE"))
//   
//   Resolver.sftp("Default Repo", host, path).as(user, keyfile)
// }