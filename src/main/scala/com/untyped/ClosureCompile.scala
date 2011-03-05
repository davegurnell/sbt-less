package com.untyped

import sbt._

import java.io.File
import java.net.URL

import scala.io.Source

import com.google.javascript.jscomp.{ Compiler, CompilerOptions, JSSourceFile }

trait ClosureCompile extends DefaultWebProject {

  // Configuration ------------------------------

  def closureSourcePath: Path = webappPath
  def closureOutputPath: Path = outputPath

  def closureJsSourceFilter: NameFilter = filter("*.js")
  def closureJsSources: PathFinder = descendents(closureSourcePath, closureJsSourceFilter)
  
  def closureManifestSourceFilter: NameFilter = filter("*.jsm") | "*.jsmanifest"
  def closureManifestSources: PathFinder = descendents(closureSourcePath, closureManifestSourceFilter)
  
  def closureTempPath: Path = closureOutputPath / "closure-temp"
  
  log.info("Closure compiler config:")
  log.info("  - closureSourcePath           : " + closureSourcePath)
  log.info("  - closureOutputPath           : " + closureOutputPath)
  log.info("  - closureJsSourceFilter       : " + closureJsSourceFilter)
  log.info("  - closureJsSources            : " + closureJsSources)
  log.info("  - closureManifestSourceFilter : " + closureManifestSourceFilter)
  log.info("  - closureManifestSources      : " + closureManifestSources)
  log.info("  - closureTempPath             : " + closureTempPath)
  
  // Implementation -----------------------------
  
  lazy val compileJs = dynamic(compileJsTasks) describedAs "Compiles Javascript manifest files"
  
  def compileJsTasks = task{ None }.dependsOn(
    closureManifestSources.get.map { new ManifestHelper(_).compileTask }.toSeq : _*)
  
  class ManifestHelper(val manifestPath: Path) {

    val outputPath: Path = Path.fromFile(manifestPath.asFile.getParentFile)
    
    // Reading the manifest ---------------------
    
    // Before we can build a JS file, we have to read its manifest,
    // chop out comments, and skip blank lines:

    def stripComments(line: String) = "#.*$".r.replaceAllIn(line, "").trim
    def isSkippable(line: String): Boolean = stripComments(line) == ""
    def isURL(line: String): Boolean = stripComments(line).matches("^https?:.*")
    
    def lines: List[String] =
      FileUtilities.readString(manifestPath.asFile, log).
                    right.
                    get.
                    split("[\r\n]+").
                    filter(item => !isSkippable(item)).
                    toList

    // URLs -------------------------------------
    
    // The first part of building a JS file is downloading and caching
    // any URLs specified in the manifest:
    
    def extendPath(path: Path, rel: String): Path =
      rel.split("[\\/]").foldLeft(path)(_ / _)
    
    def urlToFilename(line: String): String = 
      """[^A-Za-z0-9.]""".r.replaceAllIn(line, "_")
      
    def urlContent(url: URL): String =
      Source.fromInputStream(url.openStream).mkString

    def linePath(line: String): Path = {
      if(isURL(line)) {
        log.info("URL line " + line)
        log.info(" as file " + urlToFilename(line))
        val ans = closureTempPath / urlToFilename(line)
        log.info(" DONE " + ans)
        ans
      } else {
        log.info("File line " + line)
        val ans = extendPath(closureSourcePath, line)
        log.info(" DONE " + ans)
        ans
      }
    }

    def urlLines: List[String] = lines.filter(isURL _)
    def urls: List[URL] = urlLines.map(new URL(_))
    def urlPaths: List[Path] = urlLines.map(linePath _)
    
    def download(url: URL, path: Path): Option[String] = {
      log.info("Creating directory")
      FileUtilities.createDirectory(Path.fromFile(path.asFile.getParent), log)
      log.info("Writing file")
      FileUtilities.write(path.asFile, urlContent(url), log)
    }
    
    def downloadTasks: List[Task] = {
      for((url, path) <- urls.zip(urlPaths)) yield {
        val label = "Download " + url.toString
        val product = List(path) from Nil
        fileTask(label, product){
          log.info("Downloading " + url.toString + " to " + path.toString)
          download(url, path)
        }.describedAs(label)
      }
    }
    
    // Compilation ------------------------------
    
    // Once URLs have been downloaded and cached, we can run the whole file
    // through the Closure compiler:
    
    def externPaths: List[Path] = Nil
    
    def sourcePaths: List[Path] = lines.map(linePath _)

    def pathToJSSourceFile(path: Path): JSSourceFile =
      JSSourceFile.fromFile(path.asFile)

    def compile: Option[String] = {
      val compiler = new Compiler
      
      val externs = externPaths.map(pathToJSSourceFile _).toArray
      val sources = sourcePaths.map(pathToJSSourceFile _).toArray
      val options = new CompilerOptions

      val result = compiler.compile(externs, sources, options)

      FileUtilities.createDirectory(Path.fromFile(outputPath.asFile.getParent), log)
      FileUtilities.write(outputPath.asFile, compiler.toSource, log)
    }
    
    def compileTask: Task = {
      val label = "Compile " + outputPath.name
      val product = outputPath from (manifestPath :: sourcePaths)

      fileTask(label, product){
        log.info("Compiling " + outputPath.name)
        compile
      }.describedAs(label).dependsOn(downloadTasks : _*)
    }
    
  }
  
}
