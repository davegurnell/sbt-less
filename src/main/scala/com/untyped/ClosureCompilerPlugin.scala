package com.untyped

import sbt._

import java.io.File
import java.net.URL

import scala.io.Source

import com.google.javascript.jscomp.{ Compiler, CompilerOptions, JSSourceFile }

trait ClosureCompilerPlugin extends DefaultWebProject {

  // Configuration ------------------------------

  def closureSourcePath: Path = webappPath ##

  def closureJsSourceFilter: NameFilter = filter("*.js")
  def closureJsSources: PathFinder = descendents(closureSourcePath, closureJsSourceFilter)
  
  def closureManifestSourceFilter: NameFilter = filter("*.jsm") | "*.jsmanifest"
  def closureManifestSources: PathFinder = descendents(closureSourcePath, closureManifestSourceFilter)
  
  def closureOutputPath: Path = (outputPath / "closure-temp") ##
  
  log.debug("Closure compiler config:")
  log.debug("  - closureSourcePath           : " + closureSourcePath)
  log.debug("  - closureOutputPath           : " + closureOutputPath)
  log.debug("  - closureJsSourceFilter       : " + closureJsSourceFilter)
  log.debug("  - closureJsSources            : " + closureJsSources)
  log.debug("  - closureManifestSourceFilter : " + closureManifestSourceFilter)
  log.debug("  - closureManifestSources      : " + closureManifestSources)
  
  // Top-level stuff ----------------------------
  
  lazy val compileJs = dynamic(compileJsAction) describedAs "Compiles Javascript manifest files"
  
  def compileJsAction = task{ None }.dependsOn(
    closureManifestSources.get.map { new ManifestHelper(_).compileTask }.toSeq : _*)

  override def prepareWebappAction = super.prepareWebappAction.dependsOn(compileJs) 
  override def extraWebappFiles = super.extraWebappFiles +++ (closureOutputPath ** "*")
  override def webappResources = super.webappResources --- closureManifestSources
  override def watchPaths = super.watchPaths +++ closureJsSources +++ closureManifestSources

  // Implementation -----------------------------
  
  class ManifestHelper(val manifestPath: Path) {

    val outputPath: Path = toOutputPath(manifestPath)
    val directoryPath: Path = Path.fromFile(manifestPath.asFile.getParent)
    
    def toOutputPath(in: Path): Path = {
      // Put in output directory:
      val name0 = in.absolutePath.toString.
                     replace(closureSourcePath.absolutePath.toString, 
                             closureOutputPath.absolutePath.toString)
                             
      // Rename from .jsm or .jsmanifest to .js:
      val name1 = """[.]jsm(anifest)?$""".r.replaceAllIn(name0, ".js")
      
      Path.fromFile(new File(name1))
    }
    
    log.debug("JS manifest config:")
    log.debug("  - manifestPath : " + manifestPath)
    log.debug("  - outputPath   : " + outputPath)
    log.debug("  - lines        : " + lines)
    log.debug("  - urls         : " + urls)
    log.debug("  - urlPaths     : " + urlPaths)
    log.debug("  - sourcePaths  : " + sourcePaths)
    
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
    
    def urlToFilename(line: String): String = 
      """[^A-Za-z0-9.]""".r.replaceAllIn(line, "_")
      
    def urlContent(url: URL): String =
      Source.fromInputStream(url.openStream).mkString

    def linePath(line: String): Path = {
      if(isURL(line)) {
        toOutputPath(directoryPath) / urlToFilename(line)
      } else {
        Path.fromString(directoryPath, line)
      }
    }

    def urlLines: List[String] = lines.filter(isURL _)
    def urls: List[URL] = urlLines.map(new URL(_))
    def urlPaths: List[Path] = urlLines.map(linePath _)
    
    def download(url: URL, path: Path): Option[String] = {
      FileUtilities.createDirectory(Path.fromFile(path.asFile.getParent), log)
      FileUtilities.write(path.asFile, urlContent(url), log)
    }
    
    def downloadTasks: List[Task] = {
      for((url, path) <- urls.zip(urlPaths)) yield {
        val label = "Download " + url.toString
        val product = List(path) from Nil

        log.info("Downloading " + url.toString + " to " + path.toString)

        fileTask(label, product){
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

      log.info("Compiling " + manifestPath.toString + " to " + outputPath.toString)

      fileTask(label, product){
        compile
      }.describedAs(label).dependsOn(downloadTasks : _*)
    }
    
  }
  
}
