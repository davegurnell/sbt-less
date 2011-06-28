package untyped

import sbt._

import java.io.File
import java.net.URL

import scala.io.Source

import com.asual.lesscss._

trait LessCssPlugin extends DefaultWebProject {

  // Configuration ------------------------------

  def lessSourcePath: Path = webappPath

  def lessSourceFilter: NameFilter = filter("*.less")
  def lessSources: PathFinder = descendents(lessSourcePath, lessSourceFilter)
  
  def lessOutputPath: Path = (outputPath / "less-css-temp") ##
  
  val lessEngine = new LessEngine
  
  log.debug("Less CSS config:")
  log.debug("  - lessPath         : " + lessSourcePath)
  log.debug("  - lessSourceFilter : " + lessSourceFilter)
  log.debug("  - lessSources      : " + lessSources)
  log.debug("  - lessOutputPath   : " + lessOutputPath)
  
  // Top-level stuff ----------------------------
  
  lazy val compileLess = dynamic(compileLessAction) describedAs "Compiles Less CSS files"
  
  def compileLessAction = task{ None }.named("less-complete").dependsOn(
    lessSources.get.map(new LessHelper(_).compileTask).toSeq : _*)

  override def prepareWebappAction = super.prepareWebappAction.dependsOn(compileLess) 
  override def extraWebappFiles = super.extraWebappFiles +++ (lessOutputPath ** "*")
  override def webappResources = super.webappResources --- lessSources
  override def watchPaths = super.watchPaths +++ lessSources

  // Implementation -----------------------------
  
  class LessHelper(val inputPath: Path) {

    val outputPath: Path = toOutputPath(inputPath)
    val outputDirectory: Path = Path.fromFile(outputPath.asFile.getParent)
    
    def toOutputPath(in: Path): Path = {
      // Put in output directory:
      val name0 = in.absolutePath.toString.
                     replace(lessSourcePath.absolutePath.toString, 
                             lessOutputPath.absolutePath.toString)
                             
      // Rename from .jsm or .jsmanifest to .js:
      val name1 = """[.]less?$""".r.replaceAllIn(name0, ".css")
      
      Path.fromFile(new File(name1))
    }
    
    log.debug("Less CSS config:")
    log.debug("  - inputPath  : " + inputPath)
    log.debug("  - outputPath : " + outputPath)
    
    def makeOutputDirectory: Option[String] =
      FileUtilities.createDirectory(outputDirectory, log)

    def compile: Option[String] = {
      try {
        FileUtilities.write(
          outputPath.asFile,
          lessEngine.compile(inputPath.asFile),
          log)
      } catch {
        case exn: LessException => Some("LESS CSS error: " + exn.getMessage)
      }
    }
    
    def compileTask: Task = {
      val label = "less-compile " + outputPath.name
      val product = outputPath from (inputPath)

      fileTask(label, product) {
        log.debug("to " + outputPath.toString)
        makeOutputDirectory.orElse(compile)
      }.named(label)
    }
    
  }
  
}
