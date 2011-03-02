package com.untyped

import sbt._
import scala.io.Source
import java.io.File

import com.google.javascript.jscomp.{ Compiler, CompilerOptions, JSSourceFile }

class JsManifest(val manifestPath: Path, val desPath: Path) {
  
  val rootDir: Path = Path.fromFile(manifestPath.asFile.getParentFile)
  
  def readManifest(log: Logger): List[String] =
    FileUtilities.readString(manifestPath.asFile, log).
                  right.
                  get.
                  split("[\r\n]+").
                  toList

  def stripComment(line: String) =
    "#.*$".r.replaceAllIn(line, "").trim
    
  def isSkippable(line: String): Boolean =
    stripComment(line) == ""

  def isUrl(line: String): Boolean =
    stripComment(line).matches("^https?:.*")

  def isFile(line: String): Boolean =
    !isSkippable(line) && !isUrl(line)
  
  def lineToSources(line: String): List[JSSourceFile] = {
    if(isUrl(line)) {
      Nil
    } else if(isFile(line)) {
      List(JSSourceFile.fromFile(Path.fromString(rootDir, line).asFile))
    } else {
      Nil
    }
  }
  
  def externs(log: Logger): Array[JSSourceFile] = Nil.toArray
  
  def sources(log: Logger): Array[JSSourceFile] =
    readManifest(log).flatMap(lineToSources _).toArray
  
  def compile(log: Logger): Option[String] = {
    val compiler = new Compiler
    val options = new CompilerOptions
    val result = compiler.compile(externs(log), sources(log), options)
    
    log.info("Source " + compiler.toSource)
    
    new File(desPath.asFile.getParent).mkdirs()
    
    log.info("Directories created")
    
    FileUtilities.write(desPath.asFile, compiler.toSource, log)
  }
  
}