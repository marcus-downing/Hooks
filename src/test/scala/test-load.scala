package hooks.test

import org.scalatest.{Spec,GivenWhenThen,FeatureSpec}
import org.scalatest.matchers.{ShouldMatchers,MustMatchers}
import java.io.File 
import hooks._

import org.clapper.classutil._

class LoadSpec extends FeatureSpec with GivenWhenThen with MustMatchers {
  //  Utility
  def report(plugins: List[Plugin], label: String) = {
    plugins.length+" "+label+": "+plugins.sortBy(_.name).map(_.name).mkString(", ")
  }
  /*
  trait ExampleTrait

  class ExampleFeature extends Feature {
    val name = "Example"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }
  
  class ExampleFeature2 extends ExampleFeature with ExampleTrait {
	override val name = "Example 2"
  }
  
  object ExampleObject extends ExampleFeature with ExampleTrait {
    override val name = "Example Object"
  }*/

  feature("A codebase") {
    scenario("auto-load plugins") {
      val file = new File("target/scala-2.8.1.final/test-classes")
	  val file2 = new File("target/scala-2.8.1.final/classes")
      //info(file.getAbsolutePath)
      val finder = ClassFinder(List(file, file2))
      
      //info("Classes: "+finder.getClasses.toList.map(_.name).mkString(", "))
      //info("Classes: "+finder.getClasses.toList.map(_.name).filter(_.contains("Example")).mkString(", "))
	  /*
	  val exampleClasses = finder.getClasses.toList.filter(_.name.contains("Example"))
	  for(classInfo <- exampleClasses) {
		info("Class: "+classInfo.name)
		info("  Signature: "+classInfo.signature)
		info("  isConcrete: "+classInfo.isConcrete)
		if (classInfo.implements("hooks.Feature")) info("  implements Feature")
		if (classInfo.implements("hooks.Plugin")) info("  implements Plugin")
		info("  interfaces: "+classInfo.interfaces.mkString(", "))
	  }
	  
	  val classes = finder.getClasses
	  val classMap = ClassFinder.classInfoMap(classes)
	  val examples = ClassFinder.concreteSubclasses("hooks.Feature", classMap)
      info("Features: "+examples.toList.map(_.name).mkString(", "))
	  */
      //info("Features: "+finder.getClasses.toList.filter(_.implements("hooks.Features")).map(_.name).mkString(", "))
	  
      //val classes = finder.getClasses
	  //val classMap = ClassFinder.classInfoMap(classes)
      //val featureClasses = ClassFinder.concreteSubclasses("hooks.Feature", classMap)
      //info(featureClasses.length+" features")
	  

      val features = FeatureLoader(file, file2).getFeatures
      info(features.length+" loaded features")
	  //info(report(features, "loaded features"))
	  assert(features.contains(FeatureA))
    }
  }
}
