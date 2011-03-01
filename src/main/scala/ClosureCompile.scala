package com.untyped

import sbt._
import scala.io.Source
import java.io.File

import com.google.javascript.jscomp.{ Compiler, CompilerOptions, JSSourceFile }

trait ClosureCompile extends BasicScalaProject {

  protected def srcRoot = "src" / "main" / "javascript"
  
  lazy val srcPaths = (srcRoot ##) ** "*.js"

  override def watchPaths = super.watchPaths +++ srcPaths

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
    srcPaths.get.map { srcPath =>
      val srcFile = srcPath.asFile
      val desFile = new File(desRoot, "\\.js$".r.replaceFirstIn(srcPath.relativePath, ".js"))

      log.info("Src = %s" format (srcPath.toString))
      log.info("Des = %s" format (Path.fromFile(desFile).toString))

      fileTask(Path.fromFile(desFile) from srcPath) {
        log.info("Compiling %s" format (srcPath.relativePath))

        val compiler = new Compiler
        val externs = List[JSSourceFile]().toArray
        val sources = List[JSSourceFile](JSSourceFile.fromFile(srcFile)).toArray
        val options = new CompilerOptions
        
        val result = compiler.compile(externs, sources, options)

        new File(desFile.getParent).mkdirs()
        
        FileUtilities.write(desFile, compiler.toSource, log)
      }
    }
  }.toSeq: _*)
}
