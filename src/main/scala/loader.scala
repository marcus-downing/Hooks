package hooks

import org.clapper.classutil._
import java.io.{File, FileFilter, FilenameFilter}

/**
 * FeatureLoader and PluginLoader
 *
 * Load features automatically. The FeatureLoader finds all features in a given classpath.
 *
 * PluginLoader is a bit more particular: it looks for plugin files (or folders) with the right name
 * in a given directory, and expects each file to contain exactly one Plugin object.
 */

object FeatureLoader {
  def apply(classpath: File*) = new FeatureLoader(classpath.toList)

  def loadObject[T](classInfo: ClassInfo): Option[T] = {
    val name = classInfo.name
    if (name endsWith "$") {
      try {
        val cls = java.lang.Class.forName(name)
        val plugin = cls.getField("MODULE$").get(cls).asInstanceOf[T]
        Option(plugin)
      } catch { case x => None }
    } else None
  }
}

class FeatureLoader(classpath: List[File]) {
  val finder = ClassFinder(classpath)
  val classMap = ClassFinder.classInfoMap(finder.getClasses)

  def getFeatures: List[Feature] = {
    val featureClasses = ClassFinder.concreteSubclasses("hooks.Feature", classMap)
    featureClasses.toList.flatMap { case c: ClassInfo => loadFeature(c) }
  }
  
  def loadFeature(classInfo: ClassInfo): Option[Feature] = FeatureLoader.loadObject[Feature](classInfo)
  def loadPlugin(classInfo: ClassInfo): Option[Plugin] = FeatureLoader.loadObject[Plugin](classInfo)

  def registerFeatures(repo: FeatureRepository) =
    repo.register(getFeatures: _*)
    
  // plugins
  def getPlugins: List[Plugin] = {
    val pluginClasses = ClassFinder.concreteSubclasses("hooks.Plugin", classMap)
    pluginClasses.toList.flatMap { case c: ClassInfo => loadPlugin(c) }
  }
}

class PluginLoader(folder: File, classpath: List[File], suffix: String = ".jar", recurse: Boolean = false) {
  def getPlugins: List[Plugin] = {
    val pluginClasses = pluginFiles flatMap { file =>
      val finder = ClassFinder(file :: classpath)
      val classMap = ClassFinder.classInfoMap(finder.getClasses)
      val classes = ClassFinder.concreteSubclasses("hooks.Plugin", classMap)
      val pluginClasses = classes.filter(_.location == file).toList
      pluginClasses.headOption
    }
    pluginClasses flatMap ( c => loadPlugin(c) )
  }
  
  def pluginFiles: List[File] = {
    val suffixFilter = new FilenameFilter { def accept(dir: File, name: String) = name.endsWith(suffix) }
    val dirFilter = new FileFilter { def accept(file: File) = file.isDirectory() && !file.getName().endsWith(suffix) }
    
    if (recurse) {
      def recursively(dir: File): List[File] = {
        val a = dir.listFiles(dirFilter).toList.flatMap(recursively _)
        val b = folder.listFiles(suffixFilter).toList
        a ::: b
      }
      recursively(folder)
    } else
      folder.listFiles(suffixFilter).toList
  }

  def loadPlugin(classInfo: ClassInfo): Option[Plugin] = FeatureLoader.loadObject[Plugin](classInfo)
}
