package com.untyped

import sbt._
import scala.io.Source
import java.io.File

trait ClosureCompile extends BasicScalaProject {

  protected def srcRoot = "src" / "main" / "javascript"

  lazy val manifestPaths = (srcRoot ##) ** "*.jstarget"
  lazy val srcPaths = (srcRoot ##) ** "*.js"

  override def watchPaths = super.watchPaths +++ manifestPaths +++ srcPaths

  protected def desRoot = {
    if (mainArtifact.extension == "war") {
      ("src" / "main" / "webapp" / "static" / "scripts").asFile.getPath
    } else {
      ("src" / "main" / "scripts").asFile.getPath
    }
  }
  
  lazy val compileJs = dynamic(compileJsTasks) describedAs
    "Compiles Javascript files."
  
  def compileJsTasks = task { None } dependsOn ({
    manifestPaths.get.map { manifestPath =>

      val desPath = Path.fromFile(new File(
        desRoot, "\\.jstarget$".r.replaceFirstIn(manifestPath.relativePath, ".js")))
      
      val manifest: JsManifest = new JsManifest(manifestPath, desPath)

      log.info("Manifest " + manifestPath)
      log.info("Destination " + desPath)
      
      fileTask(desPath from manifestPath) {
        manifest.compile(log)
      }
    }
  }.toSeq: _*)
}
