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

  feature("A codebase") {
    scenario("auto-load plugins") {
      val version = "scala-2.9.0.final"
      val classesFolder = new File("target/"+version+"/classes")
      val testClassesFolder = new File("target/"+version+"/test-classes")
      info(classesFolder.getAbsolutePath)
      val finder = ClassFinder(List(classesFolder, testClassesFolder))
      
      //info("Classes: "+finder.getClasses.toList.map(_.name).mkString(", "))
      //info("Classes: "+finder.getClasses.toList.map(_.name).filter(_.contains("Example")).mkString(", "))
      //info("Features: "+finder.getClasses.toList.filter(_.implements("hooks.Features")).map(_.name).mkString(", "))
      
      //info("Feature class: "+finder.getClasses.toList.filter(_.name.contains("hooks.Feature")).map(_.name).mkString(", "))
      
      /*
      val classes = finder.getClasses
      val classMap = ClassFinder.classInfoMap(classes)
      val featureClasses = ClassFinder.concreteSubclasses("hooks.Feature", classMap)
      info(featureClasses.length+" features")
      */

      val features = FeatureLoader(classesFolder, testClassesFolder).getFeatures
      info(features.length+" loaded features")
      //info(report(features, "loaded features"))
      assert(features.contains(FeatureA))
    }
  }
}
