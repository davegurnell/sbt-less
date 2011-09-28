package untyped
package less

import sbt._
import scala.collection._

object LessSource {

  val importRegex = """^@import "([^"]+)";$""".r

  val lessEngine = new com.asual.lesscss.LessEngine

}

case class LessSource(val src: File, val des: File) {
  
  lazy val imports: Seq[File] = {
    val srcDirectory = src.getParent
    
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- LessSource.importRegex.findAllIn(line).matchData.map(_.group(1)).toList
    } yield new File(srcDirectory, name)
  }
  
  def compile(log: Logger): File = {
    log.info("Compiling Less CSS source %s".format(des))
    IO.write(des, LessSource.lessEngine.compile(src))
    des
  }

  def clean(log: Logger): Unit = {
    log.info("Cleaning CSS source %s".format(des))
    IO.delete(des)
  }
  
}

case class LessSources(val sources: List[LessSource]) {
  
  def sourcesRequiringRecompilation: List[LessSource] =
    sources filter (requiresRecompilation _)
  
  def requiresRecompilation(a: LessSource): Boolean =
    !a.des.exists ||
    (a.src newerThan a.des) ||
    a.imports.exists(_ newerThan a.src) ||
    ancestors(a).exists(requiresRecompilation _)
  
  def parents(a: LessSource): List[LessSource] =
    sources filter(b => a.imports.contains(b.src))

  def children(a: LessSource): List[LessSource] =
    sources filter(b => b.imports.contains(a.src))
  
  def ancestors(a: LessSource): List[LessSource] =
    breadthFirstSearch(parents _, List(a), Nil).
    filterNot(_ == a)
    
  def descendents(a: LessSource): List[LessSource] =
    breadthFirstSearch(children _, List(a), Nil).
    filterNot(_ == a)
  
  def breadthFirstSearch(succ: (LessSource) => List[LessSource], open: List[LessSource], ans: List[LessSource]): List[LessSource] =
    open match {
      case Nil =>
        ans
      
      case next :: rest =>
        if(ans.contains(next)) {
          breadthFirstSearch(succ, rest, ans)
        } else {
          breadthFirstSearch(succ, rest ::: succ(next), next :: ans)
        }
    }

  def dump(log: Logger): Unit =
    sources.foreach { source =>
      log.debug("Less CSS source:")
      
      log.debug("  src:")
      log.debug("    " + source.src)

      log.debug("  des:")
      log.debug("    " + source.des)
      
      log.debug("  recompile?:")
      log.debug("    " + requiresRecompilation(source))
      
      log.debug("  parents:")
      parents(source).foreach(src => log.debug("    " + src))
      
      log.debug("  children:")
      children(source).foreach(src => log.debug("    " + src))
      
      log.debug("  ancestors:")
      ancestors(source).foreach(src => log.debug("    " + src))
      
      log.debug("  descendents:")
      descendents(source).foreach(src => log.debug("    " + src))
    }
  
}