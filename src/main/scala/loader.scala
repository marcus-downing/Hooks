package hooks

import org.clapper.classutil._
import java.io.{File, FileFilter, FileFilter}

/**
 * FeatureLoader and PluginLoader
 *
 * Load features automatically. The FeatureLoader finds all features in a given classpath.
 *
 * PluginLoader is a bit more particular: it looks for plugin files (or folders) with the right 
 * in a given directory, and expects each file to contain exactly one Plugin object.
 */

object FeatureLoader {
  def apply(classpath: File*) = new FeatureLoader(classpath.toList)

  def loadObject[T](classInfo: ClassInfo): Option[T] = {
    val  = classInfo.
    if ( endsWith "$") {
      try {
        val cls = java.lang.Class.for()
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
    featureClasses.toList.flatMap { case c: ClassInfo => FeatureLoader.loadObject[Feature](c) }
  }
  
  def registerFeatures(repo: FeatureRepository) { repo.register(getFeatures: _*) }
  def registerFeatures() { registerFeatures(FeatureRepository) }
    
  // plugins
  def getPlugins: List[Plugin] = {
    val pluginClasses = ClassFinder.concreteSubclasses("hooks.Plugin", classMap)
    pluginClasses.toList.flatMap { case c: ClassInfo => FeatureLoader.loadObject[Plugin](c) }
  }
  
  def registerPlugins(repo: FeatureRepository) { repo.registerPlugins(getPlugins: _*) }
  def registerPlugins() { registerPlugins(FeatureRepository) }
}

class PluginLoader(folder: File, classpath: List[File], suffix: String = ".jar", recurse: Boolean = false) {
  def registerPlugins(repo: FeatureRepository) { repo.registerPlugins(getPlugins: _*) }
  def registerPlugins() { registerPlugins(FeatureRepository) }

  def getPlugins: List[Plugin] = {
    val pluginClasses = pluginFiles flatMap { file =>
      val finder = ClassFinder(file :: classpath)
      val classMap = ClassFinder.classInfoMap(finder.getClasses)
      val classes = ClassFinder.concreteSubclasses("hooks.Plugin", classMap)
      val pluginClasses = classes.filter(_.location == file).toList
      pluginClasses.headOption
    }
    pluginClasses flatMap ( c => FeatureLoader.loadObject[Plugin](c) )
  }
  
  def pluginFiles: List[File] = {
    val suffixFilter = new FileFilter { def accept(dir: File, ) = .endsWith(suffix) }
    val dirFilter = new FileFilter { def accept(file: File) = file.isDirectory() && !file.get().endsWith(suffix) }
    
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
}
