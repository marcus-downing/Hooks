package hooks.examples.ls

import hooks._
import java.io.File

/**
 * Basic Application Example
 * Uses hooks to reproduce the behaviour of the `ls` command.
 */

object LS {
  var path = "."
  def main(args: Array[String]) {
    registerFeatures()
    val flags = parseArgs(args)
    run(flags)
  }
  
  def registerFeatures () {
    PluginRepository.register(Verbose)
    PluginRepository.register(LongFormat)
    PluginRepository.register(ColourOutput)
    PluginRepository.register(HumanUnits)
    PluginRepository.register(SIUnits)
    PluginRepository.register(AllFiles)
    
    PluginRepository.require(DefaultFeatures)
  }
  
  def parseArgs(args: Array[String]): List[Feature] = {
    def next(args: List[String]): List[Feature] = {
      args match {
        case "-v" :: tail => Verbose :: next(tail)
        case "-a" :: tail => AllFiles :: next(tail)
        case "-l" :: tail => LongFormat :: next(tail)
        case "-c" :: tail => ColourOutput :: next(tail)
        case "-h" :: tail => HumanUnits :: next(tail)
        case "-s" :: tail => SIUnits :: next(tail)
        case p :: tail =>
          path = p
          next(tail)
        case Nil => Nil
      }
    }
    next(args.toList)
  }
  
  def run(flags: List[Feature]) {
    implicit val c: PluginContext = PluginRepository.makeContext(flags)
    
    begin()
    
    val dir = new File(path)
    println(dir.getAbsolutePath())
    
    if (!PluginRepository.hasFeature(AllFiles))
      fileGuard.register(f => !f.getName().startsWith("."))
    
    //val files = fileGuard(dir.list())
    
    //val formatter = formatterSelect()
  }
  
  val fileGuard = GuardHook[File]("File guard")
  val formatterSelect = SelectableHook[List[File] => String]("Formatter") { fs => fs.head }
  val unitFormatterSelect = SelectableHook[Int => String]("Unit formatter") { ufs => ufs.head }
  val filenameFormatterSelect = SelectableHook[String => String]("File name formatter") { fnfs => fnfs.head }
  
  val begin = ActionHook("Begin")
  val before = ActionHook("Before")
  val after = ActionHook("After")
  val done = ActionHook("Done")
}

object DefaultFeatures extends Feature {
  val name = "Default Features"
  def init(implicit c: PluginContextBuilder) = {
    
  }
}

//  features
object Verbose extends Feature {
  val name = "Verbose"
  def init(implicit c: PluginContextBuilder) = {
    LS.first.register
  }
}

object All files extends Feature {
  val name = "All files"
  def init(implicit c: PluginContextBuilder) = {
  
  }
}

object LongFormat extends Feature {
  val name = "Long format"
  def init(implicit c: PluginContextBuilder) = {
  
  }
}

object ColourOutput extends Feature {
  val name = "Colour output"
  def init(implicit c: PluginContextBuilder) = {
  
  }
}

object HumanUnits extends Feature {
  val name = "Human readable units"
  def init(implicit c: PluginContextBuilder) = {
  
  }
}

object SIUnits extends Feature {
  val name = "SI units"
  def init(implicit c: PluginContextBuilder) = {
  
  }
}

