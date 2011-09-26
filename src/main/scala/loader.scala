package hooks

import org.clapper.classutil._
import java.io.File

object FeatureLoader extends Logging {
  def apply(classpath: File*) = new FeatureLoader(classpath.toList)

}

class FeatureLoader(classpath: List[File]) {
  val finder = ClassFinder(classpath)
  val classMap = ClassFinder.classInfoMap(finder.getClasses)

  def getFeatures: List[Feature] = {
    val featureClasses = ClassFinder.concreteSubclasses("hooks.Feature", classMap)
    featureClasses.toList.flatMap { case c: ClassInfo => loadFeature(c) }
  }
  
  def loadFeature(classInfo: ClassInfo): Option[Feature] = {
    val name = classInfo.name
    if (name endsWith "$") {
      try {
        val cls = java.lang.Class.forName(name)
        val feature = cls.getField("MODULE$").get(cls).asInstanceOf[Feature]
        Option(feature)
      } catch {
        case x => //log(x.toString); 
          None // error means no cookie
      }
    } else None // it's not a singleton?
  }
  
  def registerFeatures(repo: PluginRepository) =
    repo.register(getFeatures: _*)
}