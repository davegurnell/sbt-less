SBT Closure Plugin
==================

[Simple Build Tool] plugin for compiling Javascript filesfrom multiple sources using Google's [Closure compiler].

Copyright (c) 2011 [Dave Gurnell] of [Untyped].

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Closure compiler]: http://code.google.com/p/closure-compiler
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

Usage
=====

First, create a `project/plugins/Plugins.scala` file and paste the following 
content into it:

    import sbt._

    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val untypedRepo = "Untyped Repo" at "http://repo.untyped.com"
      val closureCompiler = "com.untyped" % "sbt-closure" % "0.1"
    }

This will give you the ability to use the plugin in your project file. For example:

    import sbt._
    
    class MyProject(info: ProjectInfo) extends DefaultWebProject(info)
      with com.untyped.ClosureCompilerPlugin {
    
      // and so on...
    
    }

The default behaviour of the plugin is to scan your `src/main/webapp` directory
and look for files of extension `.jsmanifest`, or `.jsm` for short. These files
should contain ordered lists of JavaScript source locations. For example:

    # You can specify remote URLs...
    http://code.jquery.com/jquery-1.5.1.js
    
    # ...and paths relative to the location of the .jfm file:
    lib/foo.js
    bar.js
    
    # Bash-style single-line comments and blank lines are also supported.
    # These may be swapped for JS-style comments in the future.

The plugin compiles this in two phases: first, it downloads and caches any
remote scripts. Second, it feeds all of the specified scripts into the Closure
compiler.

If remote scripts are already cached on your filesystem, SBT won't try to
download them again. Running `sbt clean` will delete the cache.

You can change the compiler options by overriding the `closureCompilerOptions`
method. See the source for details.

Acknowledgements
================

Based on the [CoffeeScript SBT plugin], Copyright (c) 2010 Like Amdor.

Heavily influenced by the [YUI Compressor SBT plugin] by Jon Hoffman.

[CoffeeScript SBT plugin]: https://github.com/rubbish/coffee-script-sbt-plugin
[YUI Compressor SBT plugin]: https://github.com/hoffrocket

Licence
=======

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
