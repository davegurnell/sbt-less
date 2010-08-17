package org.coffeescript

import sbt._
import scala.io.Source
import org.jcoffeescript.JCoffeeScriptCompiler
import java.io.File

trait CoffeeScriptCompile extends DefaultProject {
  protected var coffeeScriptDirectoryPathFinder = "src" / "main" / "coffee-script"
  protected var coffeeScriptCompiledOuputDirectory = "src/main/generated-javascript/"

  lazy val coffeeScriptPaths = (coffeeScriptDirectoryPathFinder ##) ** "*.coffee"

  lazy val compileCoffeeScript = task {
    log.info("Compiling CoffeeScript")

    val coffeeCompiler = new JCoffeeScriptCompiler()

    coffeeScriptPaths.get.foreach {
      coffeePath =>
      val coffeeFile = coffeePath.asFile
      val jsDestination = new File(coffeeScriptCompiledOuputDirectory,
                                   "\\.coffee$".r.replaceFirstIn(coffeePath.relativePath, ".js"))

      log.info("Compiling %s" format (coffeePath.relativePath))

      val coffeeSource =  Source.fromFile(coffeeFile).mkString
      val compiledJs = coffeeCompiler.compile(coffeeSource)

      new File(jsDestination.getParent).mkdirs

      FileUtilities.write(jsDestination, compiledJs, log)
    }

    None
  }
}
