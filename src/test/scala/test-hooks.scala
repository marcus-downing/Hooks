package hooks.test

import org.scalatest.Spec

import hooks._

class HookSpec extends Spec {
	val data = List("foo", "bar", "qux", "ged", "mog", "nib", "kiv")
	
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
			it("should pass a value through unchanged") {
				val repo = PluginRepository()
				repo.require(FilterTestFeature7)
				implicit val context = repo.makeContext(Nil)
				
				val result = FilterTestFeature7.hook("foo")
				assert(result == "foo")
			}
		}
		
		describe("with simple actions") {
			it("should fire actions") {
				val repo = PluginRepository()
				repo.require(ActionTestFeature1)
				implicit val context = repo.makeContext(Nil)
				
				ActionTestFeature1.hook()
				assert(ActionTestFeature1.message == data(1))
			}
			
			it("should accept actions in overloaded short forms") {
				val repo = PluginRepository()
				repo.require(ActionTestFeature2)
				implicit val context = repo.makeContext(Nil)
				
				ActionTestFeature2.hook()
				assert(ActionTestFeature2.message == "foobarqux")
			}
			
			it("should fire actions in the right order") {
				val repo = PluginRepository()
				repo.require(ActionTestFeature3, ActionTestFeature4)
				implicit val context = repo.makeContext(Nil)
				
				ActionTestFeature3.hook()
				//println(ActionTestFeature3.message)
				assert(ActionTestFeature3.message == "bar")
			}
		}
		
		describe("with advanced actions") {
			it("should fire actions") {
				val repo = PluginRepository()
				repo.require(ActionTestFeature5)
				implicit val context = repo.makeContext(Nil)
				
				ActionTestFeature5.hook(data(1))
				assert(ActionTestFeature5.message == data(1))
			}
			
			it("should accept actions in overloaded short forms") {
				val repo = PluginRepository()
				repo.require(ActionTestFeature6)
				implicit val context = repo.makeContext(Nil)
				
				ActionTestFeature6.hook(data)
				assert(ActionTestFeature6.message == "foobarqux")
			}
			
			it("should fire actions in the right order") {
				val repo = PluginRepository()
				repo.require(ActionTestFeature7, ActionTestFeature4)
				implicit val context = repo.makeContext(Nil)
				
				ActionTestFeature7.hook(data)
				assert(ActionTestFeature7.message == "bar")
			}
		}
		
		describe("with a buffer") {
			it("should collect fragments") {
				val repo = PluginRepository()
				repo.require(BufferTestFeature1)
				implicit val context = repo.makeContext(Nil)
				
				val result = BufferTestFeature1.hook()
				assert(result.contains("foo") && result.contains("bar"))
			}
			it("should collect fragments in the right order") {
				val repo = PluginRepository()
				repo.require(BufferTestFeature2)
				implicit val context = repo.makeContext(Nil)
				
				val result = BufferTestFeature2.hook()
				assert(result == "barfoo")
			}
			it("should apply prefix, affix and infix") {
				val repo = PluginRepository()
				repo.require(BufferTestFeature3)
				implicit val context = repo.makeContext(Nil)
				
				val result = BufferTestFeature3.hook()
				assert(result == "(foo,bar)")
			}
			it("should delay calculating fragments") {
				val repo = PluginRepository()
				repo.require(BufferTestFeature4)
				implicit val context = repo.makeContext(Nil)
				
				BufferTestFeature4.message = "bar"
				val result = BufferTestFeature4.hook()
				assert(result == "bar")
			}
			it("should transform fragments to strings with a supplied function") {
				val repo = PluginRepository()
				repo.require(BufferTestFeature5)
				implicit val context = repo.makeContext(Nil)
				
				val result = BufferTestFeature5.hook()
				assert(result == "foo")
			}
			it("should apply the early filter to fragments") {
				val repo = PluginRepository()
				repo.require(BufferTestFeature6)
				implicit val context = repo.makeContext(Nil)
				
				val result = BufferTestFeature6.hook()
				assert(result == "foobar")
			}
			it("should apply the late filter to strings") {
				val repo = PluginRepository()
				repo.require(BufferTestFeature7)
				implicit val context = repo.makeContext(Nil)
				
				val result = BufferTestFeature7.hook()
				assert(result == "foobar")
			}
			it("should nest buffers") {
				val repo = PluginRepository()
				repo.require(BufferTestFeature8)
				implicit val context = repo.makeContext(Nil)
				
				val result = BufferTestFeature8.hook()
				//println("8: "+result)
				assert(result == "foobarquxged")
			}
		}
		
		describe("with a guard") {
		  it ("should approve by default") {
		    val repo = PluginRepository()
		    repo.require(GuardTestFeature1)
		    implicit val context = repo.makeContext(Nil)
		    
		    val result = GuardTestFeature1.hook("foo")
		    assert(result)
		  }
		  
		  it ("should refuse when any guard refuses") {
		    val repo = PluginRepository()
		    repo.require(GuardTestFeature2)
		    implicit val context = repo.makeContext(Nil)
		    
		    val result = GuardTestFeature2.hook("foo")
		    assert(!result)
		  }
		}
		
	}
}

object ComponentTestFeature extends Feature {
	val hook = new ComponentHook[String]("Test components")
	val name = "Component Test"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register("Foo")
		hook.register("Bar")
	}
}

//  foo, bar, qux, ged, mog, nib, kiv

object FilterTestFeature extends Feature {
	val hook = FilterHook[String]("Test filters 1")
	val name = "Filter Test"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.registerFilter(transform _)
	}
	
	def transform(value: String)(c: PluginContext) = value+"bar"
}

object FilterTestFeature2 extends Feature {
	val hook = FilterHook[String]("Test filters 2")
	val name = "Filter Test 2"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register((value, c) => value+"bar")
		hook.register(value => value+"qux")
	}
}

object FilterTestFeature3 extends Feature {
	val hook = FilterHook[String]("Test filters 3")
	val name = "Filter Test 3"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(value => value+"bar")
		hook.register(value => value+"qux")
	}
}
object FilterTestFeature4 extends Feature {
	val hook = FilterHook[String, List[String]]("Test filters 4")
	val name = "Filter Test 4"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.registerFilter(transform _)
	}
	
	def transform(value: String)(data: List[String])(c: PluginContext) = value+data(1)
}

object FilterTestFeature5 extends Feature {
	val hook = FilterHook[String, List[String]]("Test filters 5")
	val name = "Filter Test 5"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register((value, data, s) => value+data(1))
		hook.register((value, data) => value+data(2))
		hook.register(value => value+"ged")
	}
}

object FilterTestFeature6 extends Feature {
	val hook = FilterHook[String, List[String]]("Test filters 6")
	val name = "Filter Test 6"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register((value, data) => value+data(1))
		hook.register((value, data) => value+data(2))
	}
}

object FilterTestFeature7 extends Feature {
	val hook = FilterHook[String]("Test filters 7")
	val name = "Filter Test 7"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {}
}


object ActionTestFeature1 extends Feature {
	val hook = new SimpleActionHook("Test Action 1")
	def name = "Action Test Feature 1"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.registerAction(testMethod _)
	}
	
	var message = "foo"
	def testMethod(c: PluginContext) {
		message = "bar"
	}
}

object ActionTestFeature2 extends Feature {
	val hook = new SimpleActionHook("Test Action 2")
	def name = "Action Test Feature 2"
	def depend = Nil
	var message = "foo"
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(c => message = message+"bar")
		hook.register(message = message+"qux")
	}
}

object ActionTestFeature3 extends Feature {
	val hook = new SimpleActionHook("Test Action 2")
	def name = "Action Test Feature 3"
	def depend = Nil
	var message = "foo"
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(message = "bar")
	}
}

object ActionTestFeature4 extends Feature {
	def name = "Action Test Feature 4"
	def depend = Nil
	override def before = List(ActionTestFeature3)
	
	def init(implicit builder: PluginContextBuilder) {
		ActionTestFeature3.hook.register(ActionTestFeature3.message = "qux")
	}
}


object ActionTestFeature5 extends Feature {
	val hook = ActionHook[String]("Test Action 1")
	def name = "Action Test Feature 5"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.registerAction(testMethod _)
	}
	
	var message = "foo"
	def testMethod(m: String)(c: PluginContext) {
		message = m
	}
}

object ActionTestFeature6 extends Feature {
	val hook = ActionHook[List[String]]("Test Action 2")
	def name = "Action Test Feature 6"
	def depend = Nil
	var message = "foo"
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register((data, c) => message = message+data(1))
		hook.register(data => message = message+data(2))
	}
}

object ActionTestFeature7 extends Feature {
	val hook = ActionHook[List[String]]("Test Action 2")
	def name = "Action Test Feature 7"
	def depend = Nil
	var message = "foo"
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(data => message = data(1))
	}
}

object ActionTestFeature8 extends Feature {
	def name = "Action Test Feature 8"
	def depend = Nil
	override def before = List(ActionTestFeature7)
	
	def init(implicit builder: PluginContextBuilder) {
		ActionTestFeature7.hook.register(data => ActionTestFeature7.message = data(2))
	}
}

object BufferTestFeature1 extends Feature {
	val hook = BufferHook("Test buffers 1")
	def name = "Buffer Test Feature 1"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.add("foo")
		hook.add("bar")
	}
}

object BufferTestFeature2 extends Feature {
	val hook = BufferHook("Test buffers 2")
	def name = "Buffer Test Feature 2"
	def depend = List(BufferTestPlugin2A, BufferTestPlugin2B)
	
	def init(implicit builder: PluginContextBuilder) { }
}

object BufferTestPlugin2A extends Plugin {
	def name = "Buffer Test Plugin 2A"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		BufferTestFeature2.hook.add("foo")
	}
}

object BufferTestPlugin2B extends Plugin {
	def name = "Buffer Test Plugin 2B"
	def depend = Nil
	override def before = List(BufferTestPlugin2A)
	
	def init(implicit builder: PluginContextBuilder) {
		BufferTestFeature2.hook.add("bar")
	}
}

object BufferTestFeature3 extends Feature {
	val hook = BufferHook("Test buffers 2", "(", ",", ")")
	def name = "Buffer Test Feature 2"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.add("foo")
		hook.add("bar")
	}
}

object BufferTestFeature4 extends Feature {
	val hook = BufferHook("Test buffers 4")
	def name = "Buffer Test Feature 4"
	def depend = Nil
	var message = "foo"
	
	def init(implicit builder: PluginContextBuilder) {
		hook.add(message)
	}
}

object BufferTestFeature5 extends Feature {
	class Foo
	object Bar extends Foo
	val hook = BufferHook[Foo]("Test buffers 5", { foo: Foo => 
		foo match { case Bar => "foo" }
	})
	def name = "Buffer Test Feature 5"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.add(Bar)
	}
}

object BufferTestFeature6 extends Feature {
	val hook = BufferHook("Test buffers 6")
	def name = "Buffer Test Feature 6"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.add("foo")
		hook.earlyFilters.register(fr => fr+"bar")
	}
}

object BufferTestFeature7 extends Feature {
	val hook = BufferHook("Test buffers 7")
	def name = "Buffer Test Feature 7"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.add("foo")
		hook.lateFilters.register(fr => fr+"bar")
	}
}

object BufferTestFeature8 extends Feature {
	val hook = BufferHook("Test buffers 8")
	val innerhook = BufferHook("Test buffers 8 (inner)")
	def name = "Buffer Test Feature 8"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.add("foo")
		hook.add(innerhook)
		hook.add("ged")
		innerhook.add("bar")
		innerhook.add("qux")
	}
}

object GuardTestFeature1 extends Feature {
	val hook = GuardHook[String]("Test guards 1")
	def name = "Guard Test Feature 1"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
	}
}

object GuardTestFeature2 extends Feature {
	val hook = GuardHook[String]("Test guards 2")
	def name = "Guard Test Feature 2"
	def depend = Nil
	
	def init(implicit builder: PluginContextBuilder) {
		hook.register(m => m == "foo")
		hook.register(m => m == "bar")
	}
}

