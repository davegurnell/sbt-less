SBT Less CSS Plugin
===================

[Simple Build Tool] plugin for compiling [Less CSS] files.

Copyright (c) 2011 [Dave Gurnell] of [Untyped].

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Less CSS]: http://lesscss.org
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

Usage
=====

First, create a `project/plugins/Plugins.scala` file and paste the following 
content into it:

    import sbt._

    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val untypedRepo = "Untyped Repo" at "http://repo.untyped.com"
      val lessCompiler = "com.untyped" % "sbt-less" % "0.1"
    }

This will give you the ability to use the plugin in your project file. For example:

    import sbt._
    
    class MyProject(info: ProjectInfo) extends DefaultWebProject(info)
      with com.untyped.LessCssPlugin {
    
      // and so on...
    
    }

The default behaviour of the plugin is to scan your `src/main/webapp` directory
during `prepare-webapp` and compile any `.less` to `.css` files. You need to have
the `lessc` command available on your path for the plugin to work.

Acknowledgements
================

Based indirectly on the [Coffee Script SBT plugin], Copyright (c) 2010 Luke Amdor.

Heavily influenced by the [YUI Compressor SBT plugin] by Jon Hoffman.

[Coffee Script SBT plugin]: https://github.com/rubbish/coffee-script-sbt-plugin
[YUI Compressor SBT plugin]: https://github.com/hoffrocket/sbt-yui

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
