package untyped
package less

import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {

  object LessKeys {
    val less = TaskKey[List[File]]("less", "Compile Less CSS sources.")
    val lessSources = TaskKey[LessSources]("less-sources", "List of Less CSS sources.")
    val filter = SettingKey[FileFilter]("filter", "Filter for selecting Less CSS sources from default directories.")
    val excludeFilter = SettingKey[FileFilter]("exclude-filter", "Filter for exclusing Less CSS sources from default diretories.")
  }
  
  import LessKeys._
  
  def cssFileOf(file: File, sourceDir: File, targetDir: File) =
    Some(new File(targetDir, IO.relativize(sourceDir, file).get.replace(".less", ".css")))
  
  def lessSourcesTask: Initialize[Task[LessSources]] =
    (streams, sourceDirectory in less, resourceManaged in less, filter in less, excludeFilter in less) map {
      (out, sourceDir, targetDir, filter, excludeFilter) =>
        val sources =
          for {
            src <- sourceDir.descendentsExcept(filter, excludeFilter).get
            des <- cssFileOf(src, sourceDir, targetDir)
          } yield LessSource(src, des)
        
        LessSources(sources.toList)
    }
  
  def lessCompilerTask =
    (streams, lessSources in less) map {
      (out, lessSources: LessSources) =>
        lessSources.dump(out.log)
        
        lessSources.sourcesRequiringRecompilation match {
          case Nil =>
            out.log.info("No Less CSS sources requiring compilation")
            Nil
          
          case toCompile =>
            for {
              source <- toCompile
            } yield source.compile(out.log)
        }
    }
  
  def lessCleanTask =
    (streams, lessSources in less) map {
      (out, lessSources) =>
        for {
          source <- lessSources.sources
        } source.clean(out.log)
    }

  def lessSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      filter in less := "*.less",
      excludeFilter in less := (".*" - ".") || HiddenFileFilter,
      sourceDirectory in less <<= (sourceDirectory in conf),
      resourceManaged in less <<= (resourceManaged in conf),
      lessSources in less <<= lessSourcesTask,
      clean in less <<= lessCleanTask,
      less <<= lessCompilerTask))
  
  def lessSettings: Seq[Setting[_]] =
    lessSettingsIn(Compile) ++
    lessSettingsIn(Test)
    
}
