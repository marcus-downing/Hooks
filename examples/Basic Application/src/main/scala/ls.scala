package hooks.examples.ls

import hooks._
import scala.collection.mutable.ListBuffer
import java.io.File
import java.text.SimpleDateFormat
import org.apache.commons.io.FileUtils
import jline.{Terminal,ANSIBuffer}

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
  val filenameFilter = FilterHook[String, File]("Filenames")
  
  //  application
  def registerFeatures () {
    FeatureRepository.require(DefaultFeatures)
    FeatureRepository.register(Verbose, LongFormat, ColourOutput, HumanUnits, SIUnits, AllFiles)
  }
  
  def parseArgs(args: Array[String]): List[Feature] = {
    var features = new ListBuffer[Feature]()
    args foreach { arg =>
      arg match {
        case "-v"  => features += Verbose
        case "-a"  => features += AllFiles
        case "-l"  => features += LongFormat
        case "-c"  => features += ColourOutput
        case "-h"  => features += HumanUnits
        case "-si" => features += SIUnits
        case p     => path = p
      }
    }
    features.toList
  }
  
  def run(flags: List[Feature]) = FeatureRepository.usingFeatures(flags) {
    println()
    if (Verbose.isActive) println("Features: "+HookContext.get.features.map(_.name).mkString(", "))
    
    val dir = new File(path).getCanonicalFile()
    if (Verbose.isActive) println(dir.getAbsolutePath())
      
    val files = fileGuard(dir.listFiles().toList)
    println(if (HookContext.get.hasFeature(LongFormat))
      longFormat(files)
    else
      shortFormat(files))
  }
  
  val dateFormat = new SimpleDateFormat("d MMM yyyy")
  val timeFormat = new SimpleDateFormat("H:mm:ss")
    
  def longFormat(files: List[File]): String = {
    case class Item(name: String, ftype: String, permissions: String, size: String, date: String, time: String)
    val items = files map { file =>
      val name = filenameFilter(file.getName(), file)
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
        else if (HumanUnits.isActive) HumanUnits(file.length()) 
        else if (SIUnits.isActive) SIUnits(file.length()) 
        else file.length().toString()
      
      Item(name, ftype, permissions, size, date, time)
    }
    
    val sizepad = items.map(_.size.length).max
    val datepad = items.map(_.date.length).max
    val timepad = items.map(_.time.length).max
    
    val lines = for (item <- items) yield {
      val paddedSize = padl(item.size, sizepad)
      val paddedDate = padl(item.date, datepad)
      val paddedTime = padl(item.time, timepad)
      "  "+item.ftype+item.permissions+"   "+paddedDate+" "+paddedTime+"   "+paddedSize+"   "+item.name
    }
    lines.mkString("\n")
  }
  
  def shortFormat(files: List[File]): String = {
    val terminal = Terminal.getTerminal()
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
    if (!AllFiles.isActive)
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
    val terminal = Terminal.getTerminal()
    if (terminal.isANSISupported()) {
      LS.filenameFilter.register { (name, file) =>
        import ANSIBuffer.ANSICodes._
        if (Verbose.isActive) println("Colours for "+name)
        if (FileUtils.isSymlink(file))
          attrib(36)+name+attrib(37)
        else if (file.isDirectory())
          attrib(32)+name+attrib(37)
        else
          name
      }
    } else if (Verbose.isActive) println("Cannot show colours")
  }
}

object HumanUnits extends Feature("Human readable units", depend=List(LongFormat)) {
  def init() {
  }
  
  def apply(len: Long) = FileUtils.byteCountToDisplaySize(len).replaceAll("KB", "kB")
}

object SIUnits extends Feature("SI units", depend=List(LongFormat)) {
  def init() {
  }
  
  def apply(len: Long): String = {
    val units = Array(" bytes", " kB", " MB", " GB", " TB")
    def scaleOf(len: Long): Int = if (len >= 1000L) 1+scaleOf(len / 1000L) else 0
    val scale = scaleOf(len)
    if (Verbose.isActive) println("Number scale: "+scale)
    val mul = math.pow(1000L, scale)
    val num = {
      val num = String.format("%.3g", Double.box(len.toDouble/mul))
      num.replaceAll("0$|^0", "").replaceAll("\\.$", "")
    }

    num+units(scale)
  }
}

