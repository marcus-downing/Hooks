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
		
		describe("with simple filters") {
			it("should filter values") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature.hook("foo")
				//println(result)
				assert(result == "foobar")
			}
			it("should accept filters in overloaded short forms") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature2)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature2.hook("foo")
				//println(result)
				assert(result.contains("bar") && result.contains("qux"))
			}
			it("should apply filters in the right order") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature3)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature3.hook("foo")
				//println(result)
				assert(result == "foobarqux")
			}
		}
		
		describe("with advanced filters") {
			val data = List("foo", "bar", "qux", "ged", "mog", "nib", "kiv")
			it("should filter values") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature4)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature4.hook("foo")(data)
				assert(result == "foobar")
			} 
			it("should accept filters in overloaded short forms") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature5)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature5.hook("foo")(data)
				//println(result)
				assert(result.contains("bar") && result.contains("qux") && result.contains("ged"))
			}
			it("should apply filters in the right order") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature6)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature6.hook("foo")(data)
				//println(result)
				assert(result == "foobarqux")
			}
		}
		
		describe("with actions") {
			it("should fire actions") {
				val repo = PluginRepository()
				repo.require(ActionTestFeature)
				implicit val context = repo.makeContext(Nil)
				
				ActionTestFeature.hook("woot")
				assert(ActionTestFeature.message == "woot")
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

//  foo, bar, qux, ged, mog, nib, kiv

object FilterTestFeature extends Feature {
	val hook = FilterHook[String]("Test filters 1")
	val name = "Filter Test"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.registerFilter(transform _)
	}
	
	def transform(value: String)(c: PluginContext) = value+"bar"
}

object FilterTestFeature2 extends Feature {
	val hook = FilterHook[String]("Test filters 2")
	val name = "Filter Test 2"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register((value, c) => value+"bar")
		hook.register(value => value+"qux")
	}
}

object FilterTestFeature3 extends Feature {
	val hook = FilterHook[String]("Test filters 3")
	val name = "Filter Test 3"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(value => value+"bar")
		hook.register(value => value+"qux")
	}
}
object FilterTestFeature4 extends Feature {
	val hook = FilterHook[String, List[String]]("Test filters 4")
	val name = "Filter Test 4"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.registerFilter(transform _)
	}
	
	def transform(value: String)(data: List[String])(c: PluginContext) = value+data(1)
}

object FilterTestFeature5 extends Feature {
	val hook = FilterHook[String, List[String]]("Test filters 5")
	val name = "Filter Test 5"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register((value, data, s) => value+data(1))
		hook.register((value, data) => value+data(2))
		hook.register(value => value+"ged")
	}
}

object FilterTestFeature6 extends Feature {
	val hook = FilterHook[String, List[String]]("Test filters 6")
	val name = "Filter Test 6"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register((value, data) => value+data(1))
		hook.register((value, data) => value+data(2))
	}
}


object ActionTestFeature extends Feature {
	val hook = new ActionHook[String]("Test Action")
	def name = "Test Feature"
	def require = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.registerAction(testMethod _)
	}
	
	var message = "meh"
	def testMethod(m: String)(c: PluginContext) {
		message = m
	}
}
