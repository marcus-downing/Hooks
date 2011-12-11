package hooks.test

import org.scalatest.{Spec,GivenWhenThen,FeatureSpec}
import org.scalatest.matchers.{ShouldMatchers,MustMatchers}
import scala.util.DynamicVariable
import hooks._
//import Hooks._

/*
  Test the use of DynamicVariable
*/

class VarSpec extends FeatureSpec with GivenWhenThen with MustMatchers {
  feature("A dynamic variable") {
  
    scenario("Stores and retrieves a value") {
      val dyn = new DynamicVariable[String]("foo")
      
      def testValue() {
        dyn { value => assert(value == "bar") }
      }
      
      dyn.withValue("bar") { testValue() }
    }
  }
  
  feature("A context") {
    scenario("Store and retrieve the context") {
      def testContext() {
        HookContext { cx => assert(cx.hasFeature(FeatureA)) }
      }
      
      val repo = FeatureRepository()
      repo.require(FeatureA)
      val cx = repo.makeContext(Nil)
      assert(cx.hasFeature(FeatureA))
      cx.using { testContext(); }
    }
  }
}