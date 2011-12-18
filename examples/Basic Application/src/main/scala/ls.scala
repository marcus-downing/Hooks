package hooks.examples.ls

import hooks._
import java.io.File
import java.text.SimpleDateFormat
import org.apache.commons.io.FileUtils

/**
 * Basic Application Example
 * Uses hooks to reproduce the behaviour of the `ls` command.
 */

object LS {
  var path = "."
  
  def main(args: Array[String]) {
    try {
      registerFeatures()
      val flags = parseArgs(args)
      run(flags)
    } catch { case x => x.printStackTrace }
  }
  
  // hooks
  val fileGuard = GuardHook[File]("File guard")
  //val formatterSelect = SelectableHook[(Int, List[File] => String)]("Formatter") { fs => fs.head }
  //val unitFormatterSelect = SelectableHook[Int => String]("Unit formatter") { ufs => Some(ufs.head) }
  //val filenameFormatterSelect = SelectableHook[String => String]("File name formatter") { fnfs => Some(fnfs.head) }
  
  //val unitFormat = FilterHook[S
  val filenameFilter = FilterHook[String]("Filenames")
  
  val begin = ActionHook.simple("Begin")
  val before = ActionHook.simple("Before")
  val after = ActionHook.simple("After")
  val end = ActionHook.simple("Done")  
  
  //  application
  def registerFeatures () {
    FeatureRepository.require(DefaultFeatures)
    FeatureRepository.register(Verbose, LongFormat, ColourOutput, HumanUnits, SIUnits, AllFiles)
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
  
  def run(flags: List[Feature]) = FeatureRepository.usingFeatures(flags) {
    begin()
    
    val dir = new File(path).getCanonicalFile()
    println()
    //if (HookContext.get.hasFeature(LongFormat)) println(dir.getAbsolutePath())
      
    val files = fileGuard(dir.listFiles().toList)
    
    println(if (HookContext.get.hasFeature(LongFormat))
      longFormat(files)
    else
      shortFormat(files))
    
    end()
  }
  
  val dateFormat = new SimpleDateFormat("d MMM yyyy")
  val timeFormat = new SimpleDateFormat("HH:mm:ss")
    
  def longFormat(files: List[File]): String = {
    case class Item(name: String, ftype: String, permissions: String, size: String, date: String, time: String)
    val items = files map { file =>
      val name = filenameFilter(file.getName())
      //val isLink = file.getCanonicalPath() != file.getAbsolutePath()
      val isLink = FileUtils.isSymlink(file)
      val isDirectory = file.isDirectory()
      
      val ftype = file match {
        case _ if isLink => "l"
        case _ if isDirectory => "d"
        case _ if file.isHidden() => "h"
        case _ => "-"
      }
      
      val permissions = 
        (if (file.canRead()) "r" else "-")+
        (if (file.canWrite()) "w" else "-")+
        (if (file.canExecute()) "x" else "-")
      
      val mod = file.lastModified()
      val date = dateFormat.format(mod)
      val time = timeFormat.format(mod)
      
      val size = if (isDirectory) "-"
        else if (HookContext.get.hasFeature(HumanUnits)) HumanUnits(file.length()) 
        else file.length().toString()
      
      Item(name, ftype, permissions, size, date, time)
    }
    
    val sizepad = items.map(_.size.length).max
    val datepad = items.map(_.date.length).max
    val timepad = items.map(_.time.length).max
    
    val lines = for (item <- items) yield {
      val paddedSize = padl(item.size, sizepad)
      val paddedDate = padl(item.date, datepad)
      val paddedTime = padr(item.time, timepad)
      "  "+item.ftype+item.permissions+"   "+paddedDate+" "+paddedTime+"   "+paddedSize+"   "+item.name
    }
    lines.mkString("\n")
  }
  
  def shortFormat(files: List[File]): String = {
    val terminal = jline.Terminal.getTerminal()
    val width = terminal.getTerminalWidth()
    
    val names = files.map(_.getName())
    val colsize = names.map(_.length).max+2
    val rowlen = (width / colsize).floor
    
    
    val paddedNames = names.map(n => padr(n, colsize))
    
    paddedNames.mkString("")
  }
  
  def padl(s: String, len: Int) = String.format("%1$#"+len+"s", s)
  def padr(s: String, len: Int) = String.format("%1$-"+len+"s", s)
}

//  features
object DefaultFeatures extends Feature("Default Features") {
  def init() {
    if (!ContextBuilder.get.hasFeature(AllFiles))
      LS.fileGuard.register(f => !f.getName().startsWith("."))
  }
}

object Verbose extends Feature("Verbose") {
  def init() {
  }
}

object AllFiles extends Feature("All files") {
  def init() {
  }
}

object LongFormat extends Feature("Long format") {
  def init() {
  }
}

object ColourOutput extends Feature("Colour output") {
  def init() {
  }
}

object HumanUnits extends Feature("Human readable units") {
  def init() {
  }
  
  def apply(len: Long) = FileUtils.byteCountToDisplaySize(len)
}

object SIUnits extends Feature("SI units") {
  def init() {
  }
}

