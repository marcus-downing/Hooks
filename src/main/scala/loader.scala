package hooks


import org.clapper.classutil._
import java.io.File

object FeatureLoader extends Logging {
	def getFeatures(directory: File): List[Feature] = getFeatures(List(directory))
	def getFeatures(classpath: List[File]): List[Feature] = getFeatures(ClassFinder(classpath))

	def getFeatures(finder: ClassFinder): List[Feature] = {
		val classes = finder.getClasses
		val featureClasses = ClassFinder.concreteSubclasses("hooks.Feature", classes)
		featureClasses.flatMap { c => loadFeature(c) }
	}
	
	def loadFeature(classInfo: ClassInfo): Option[Feature] = {
		val name = classInfo.name
		if (name endsWith "$") {
			try {
				val cls = java.lang.Class.forName(name)
				val feature = cls.getField("MODULE$").get(cls).asInstanceOf[Feature]
				Option(feature)
			} catch {
				case x => log(x.toString); None // error means no cookie
			}
		} else None // it's not a singleton?
	}
	
	def registerFeatures(classpath: List[File], repo: PluginRepository) =
		repo.register(getFeatures(classpath): _*)
}