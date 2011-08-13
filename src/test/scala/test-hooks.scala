import org.scalatest.Spec

import hooks._

class HookSpec extends Spec {
	describe("A plugin") {
		describe("with components") {
			it("should store components") {
				val repo = PluginRepository()
				repo.require(ComponentTestFeature)
				implicit val context = repo.makeContext(Nil)
				
				val strings = ComponentTestFeature.hook.get
				assert(strings.contains("Foo") && strings.contains("Bar") && strings.length == 2)
			}
		}
		
		describe("with filters") {
			it("should filter values") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature.hook("foo")()
				println(result)
				assert(result == "foobar")
			}
			it("should filter values in the right order") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature2)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature2.hook("foo")()
				println(result)
				assert(result == "foobarqux")
			}
		}
		
		describe("with actions") {
			it("should fire actions") {
				/*
				val repo = PluginRepository()
				repo.require(ActionTestFeature)
				implicit val context = repo.makeContext(Nil)
				
				ActionTestFeature.hook("woot")
				assert(ActionTestFeature.message == "woot")
				*/
			}
		}
	}
}

object ComponentTestFeature extends Feature {
	val hook = new ComponentHook[String]("Test components")
	val name = "Component Test"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register("Foo")
		hook.register("Bar")
	}
}

object FilterTestFeature extends Feature {
	val hook = new FilterHook[String, Unit]("Test filters")
	val name = "Filter Test"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(transform _)
	}
	
	def transform(value: String)(u: Unit)(c: PluginContext) = value+"bar"
}

object FilterTestFeature2 extends Feature {
	val hook = new FilterHook[String, Unit]("Test filters")
	val name = "Filter Test"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(transform _)
		hook.register(value => (s => (c => value+"qux")))
	}
	
	def transform(value: String)(u: Unit)(c: PluginContext) = value+"bar"
}

/*
object ActionTestFeature extends Feature {
	val hook = new ActionHook[String]("Test Action")
	def name = "Test Feature"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(testMethod _)
	}
	
	var message = "meh"
	def testMethod(m: String)(implicit c: PluginContext) {
		message = m
	}
}*/
