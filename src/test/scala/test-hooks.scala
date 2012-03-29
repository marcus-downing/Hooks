package hooks.test

import org.scalatest.Spec

import hooks._
//import Hooks._

class HookSpec extends Spec {
  val data = List("foo", "bar", "qux", "ged", "mog", "nib", "kiv")
  
  
  describe("A feature") {
    describe("with components") {
      it("should store components") {
        val repo = FeatureRepository()
        repo.require(ComponentTestFeature)
        repo.makeContext(Nil).using {
          val strings = ComponentTestFeature.hook._get
          println("#19: "+strings.mkString(", "))
          assert(strings.contains("foo") && strings.contains("bar") && strings.length == 2)
        }
      }
      
      it("should store components standalone") {
        val repo = FeatureRepository()
        repo.require(ComponentTestFeature)
        repo.makeContext(Nil).using {
          val strings = ComponentTestFeature.hook._get
          println("#19: "+strings.mkString(", "))
          assert(strings.contains("foo") && strings.contains("bar") && strings.length == 2)
        }
      }
    }
    
    describe("with simple filters") {
      it("should filter values") {
        val repo = FeatureRepository()
        repo.require(FilterTestFeature)
        repo.makeContext(Nil).using {        
          val result = FilterTestFeature.hook("foo")
          println("#31: "+result)
          assert(result == "foobar")
        }
      }
      it("should accept filters in overloaded short forms") {
        val repo = FeatureRepository()
        repo.require(FilterTestFeature2)
        repo.makeContext(Nil).using {
          val result = FilterTestFeature2.hook("foo")
          println("#40: "+result)
          assert(result.contains("bar") && result.contains("qux"))
        }
      }
      it("should apply filters in the right order") {
        val repo = FeatureRepository()
        repo.require(FilterTestFeature3)
        repo.makeContext(Nil).using {
          val result = FilterTestFeature3.hook("foo")
          println("#49: "+result)
          assert(result == "foobarqux")
        }
      }
    }
    
    describe("with advanced filters") {
      it("should filter values") {
        val repo = FeatureRepository()
        repo.require(FilterTestFeature4)
        repo.makeContext(Nil).using {
          val result = FilterTestFeature4.hook("foo", data)
          println("#61: "+result)
          assert(result == "foobar")
        }
      } 
      it("should accept filters in overloaded short forms") {
        val repo = FeatureRepository()
        repo.require(FilterTestFeature5)
        repo.makeContext(Nil).using {        
          val result = FilterTestFeature5.hook("foo", data)
          println("#70: "+result)
          assert(result.contains("bar") && result.contains("qux") && result.contains("ged"))
        }
      }
      it("should apply filters in the right order") {
        val repo = FeatureRepository()
        repo.require(FilterTestFeature6)
        repo.makeContext(Nil).using {        
          val result = FilterTestFeature6.hook("foo", data)
          println("#79: "+result)
          assert(result == "foobarqux")
        }
      }
      it("should pass a value through unchanged") {
        val repo = FeatureRepository()
        repo.require(FilterTestFeature7)
        repo.makeContext(Nil).using {
          val result = FilterTestFeature7.hook("foo")
          println("#88: "+result)
          assert(result == "foo")
        }
      }
    }
    
    describe("with simple actions") {
      it("should fire actions") {
        val repo = FeatureRepository()
        repo.require(ActionTestFeature1)
        repo.makeContext(Nil).using {
          ActionTestFeature1.hook()
          println("#100: "+ActionTestFeature1.message)
          assert(ActionTestFeature1.message == data(1))
        }
      }
      
      it("should accept actions in overloaded short forms") {
        val repo = FeatureRepository()
        repo.require(ActionTestFeature2)
        repo.makeContext(Nil).using {
          ActionTestFeature2.hook()
          println("#110: "+ActionTestFeature2.message)
          assert(ActionTestFeature2.message == "foobarqux")
        }
      }
      
      it("should fire actions in the right order") {
        val repo = FeatureRepository()
        repo.require(ActionTestFeature3, ActionTestFeature4)
        repo.makeContext(Nil).using {
          ActionTestFeature3.hook()
          println("#120: "+ActionTestFeature3.message)
          assert(ActionTestFeature3.message == "bar")
        }
      }
    }
    
    describe("with advanced actions") {
      it("should fire actions") {
        val repo = FeatureRepository()
        repo.require(ActionTestFeature5)
        repo.makeContext(Nil).using {        
          ActionTestFeature5.hook(data(1))
          println("#132: "+ActionTestFeature5.message)
          assert(ActionTestFeature5.message == data(1))
        }
      }
      
      it("should accept actions in overloaded short forms") {
        val repo = FeatureRepository()
        repo.require(ActionTestFeature6)
        repo.makeContext(Nil).using {
          ActionTestFeature6.hook(data)
          println("#142: "+ActionTestFeature6.message)
          assert(ActionTestFeature6.message == "foobarqux")
        }
      }
      
      it("should fire actions in the right order") {
        val repo = FeatureRepository()
        repo.require(ActionTestFeature7, ActionTestFeature4)
        repo.makeContext(Nil).using {
          ActionTestFeature7.hook(data)
          println("#152: "+ActionTestFeature7.message)
          assert(ActionTestFeature7.message == "bar")
        }
      }
    }
    
    describe("with a buffer") {
      it("should collect fragments") {
        val repo = FeatureRepository()
        repo.require(BufferTestFeature1)
        repo.makeContext(Nil).using {
          val result = BufferTestFeature1.hook()
          println("#164: "+result)
          assert(result.contains("foo") && result.contains("bar"))
        }
      }
      it("should collect fragments in the right order") {
        val repo = FeatureRepository()
        repo.require(BufferTestFeature2)
        repo.makeContext(Nil).using {
          val result = BufferTestFeature2.hook()
          println("#173: "+result)
          assert(result == "barfoo")
        }
      }
      it("should apply prefix, affix and infix") {
        val repo = FeatureRepository()
        repo.require(BufferTestFeature3)
        repo.makeContext(Nil).using {
          val result = BufferTestFeature3.hook()
          println("#182: "+result)
          assert(result == "(foo,bar)")
        }
      }
      it("should delay calculating fragments") {
        val repo = FeatureRepository()
        repo.require(BufferTestFeature4)
        repo.makeContext(Nil).using {
          BufferTestFeature4.message = "bar"
          val result = BufferTestFeature4.hook()
          println("#192: "+result)
          assert(result == "bar")
        }
      }
      it("should transform fragments to strings with a supplied function") {
        val repo = FeatureRepository()
        repo.require(BufferTestFeature5)
        repo.makeContext(Nil).using {
          val result = BufferTestFeature5.hook()
          println("#201: "+result)
          assert(result == "foo")
        }
      }
      it("should apply the early filter to fragments") {
        val repo = FeatureRepository()
        repo.require(BufferTestFeature6)
        repo.makeContext(Nil).using {
          val result = BufferTestFeature6.hook()
          println("#210: "+result)
          assert(result == "foobar")
        }
      }
      it("should apply the late filter to strings") {
        val repo = FeatureRepository()
        repo.require(BufferTestFeature7)
        repo.makeContext(Nil).using {
          val result = BufferTestFeature7.hook()
          println("#219: "+result)
          assert(result == "foobar")
        }
      }
      it("should nest buffers") {
        val repo = FeatureRepository()
        repo.require(BufferTestFeature8)
        repo.makeContext(Nil).using {
          val result = BufferTestFeature8.hook()
          println("#228: "+result)
          assert(result == "foobarquxged")
        }
      }
    }
    
    describe("with a guard") {
      it ("should approve by default") {
        val repo = FeatureRepository()
        repo.require(GuardTestFeature1)
        repo.makeContext(Nil).using {
          val result = GuardTestFeature1.hook("foo")
          println("#240: "+result)
          assert(result)
        }
      }
      
      it ("should refuse when any guard refuses") {
        val repo = FeatureRepository()
        repo.require(GuardTestFeature2)
        repo.makeContext(Nil).using {
          val result = GuardTestFeature2.hook("foo")
          println("#250: "+result)
          assert(!result)
        }
      }
    }
    
  }
}

object ComponentTestFeature extends Feature("Component Test") {
  val hook = ComponentHook[String]()
  
  def init() {
    hook.hook("foo")
    hook.hook("bar")
  }
}

object ComponentTestFeature2 extends Feature("Component Test 2") {
  val hook = ComponentHook.standalone[String]()
  hook.hook("foo")
  hook.hook("bar")
  
  def init() {
  }
}

//  foo, bar, qux, ged, mog, nib, kiv

object FilterTestFeature extends Feature("Filter Test") {
  val hook = FilterHook[String]()
  
  def init() {
    hook.hook(transform _)
  }
  
  def transform(value: String) = value+"bar"
}

object FilterTestFeature2 extends Feature("Filter Test 2") {
  val hook = FilterHook[String]()
  
  def init() {
    hook.hook((value: String) => value+"bar")
    hook.hook(value => value+"qux")
  }
}

object FilterTestFeature3 extends Feature("Filter Test 3") {
  val hook = FilterHook[String]()
  
  def init() {
    hook.hook(value => value+"bar")
    hook.hook(value => value+"qux")
  }
}
object FilterTestFeature4 extends Feature("Filter Test 4") {
  val hook = FilterHook[String, List[String]]()
  
  def init() {
    hook.hook(transform _)
  }
  
  def transform(value: String, data: List[String]) = value+data(1)
}

object FilterTestFeature5 extends Feature("Filter Test 5") {
  val hook = FilterHook[String, List[String]]()
  
  def init() {
    hook.hook((value, data) => value+data(1))
    hook.hook((value: String, data: List[String]) => value+data(2))
    hook.hook(value => value+"ged")
  }
}

object FilterTestFeature6 extends Feature("Filter Test 6") {
  val hook = FilterHook[String, List[String]]()
  
  def init() {
    hook.hook((value: String, data: List[String]) => value+data(1))
    hook.hook((value: String, data: List[String]) => value+data(2))
  }
}

object FilterTestFeature7 extends Feature("Filter Test 7") {
  val hook = FilterHook[String]()
  
  def init() {}
}


object ActionTestFeature1 extends Feature("Action Test 1") {
  val hook: ActionHook0 = ActionHook.simple()
  
  def init() {
    hook.hook(testMethod)
  }
  
  var message = "foo"
  def testMethod() {
    message = "bar"
  }
}

object ActionTestFeature2 extends Feature("Action Test 2") {
  val hook = ActionHook.simple()
  var message = "foo"
  
  def init() {
    hook.hook { message = message+"bar" }
    hook.hook(message = message+"qux")
  }
}

object ActionTestFeature3 extends Feature("Action Test 3") {
  val hook = ActionHook.simple()
  var message = "foo"
  
  def init() {
    hook.hook(message = "bar")
  }
}

object ActionTestFeature4 extends Feature("Action Test 4", before = List(ActionTestFeature3)) {
  
  def init() {
    ActionTestFeature3.hook.hook(ActionTestFeature3.message = "qux")
  }
}


object ActionTestFeature5 extends Feature("Action Test 5") {
  val hook = ActionHook[String]()
  
  def init() {
    hook.hook(testMethod _)
  }
  
  var message = "foo"
  def testMethod(m: String) {
    message = m
  }
}

object ActionTestFeature6 extends Feature("Action Test 6") {
  val hook = ActionHook[List[String]]()
  var message = "foo"
  
  def init() {
    hook.hook((data) => message = message+data(1))
    hook.hook { data: List[String] => message = message+data(2) }
  }
}

object ActionTestFeature7 extends Feature("Action Test 7") {
  val hook = ActionHook[List[String]]()
  var message = "foo"
  
  def init() {
    hook.hook { data: List[String] => message = data(1) }
  }
}

object ActionTestFeature8 extends Feature("Action Test 8", before = List(ActionTestFeature7)) {
  
  def init() {
    ActionTestFeature7.hook.hook { data: List[String] => ActionTestFeature7.message = data(2) }
  }
}

object BufferTestFeature1 extends Feature("Buffer Test 1") {
  val hook = BufferHook()
  
  def init() {
    hook.add("foo")
    hook.add("bar")
  }
}

object BufferTestFeature2 extends Feature("Buffer Test 2", depend = List(BufferTestFeature2A, BufferTestFeature2B)) {
  val hook = BufferHook()
  
  def init() { }
}

object BufferTestFeature2A extends FeatureLike("Buffer Test 2A") {
  def init() {
    BufferTestFeature2.hook.add("foo")
  }
}

object BufferTestFeature2B extends FeatureLike("Buffer Test 2A", before = List(BufferTestFeature2A)) {
  def init() {
    BufferTestFeature2.hook.add("bar")
  }
}

object BufferTestFeature3 extends Feature("Buffer Test 3") {
  val hook = BufferHook("(", ",", ")")
  
  def init() {
    hook.add("foo")
    hook.add("bar")
  }
}

object BufferTestFeature4 extends Feature("Buffer Test 4") {
  val hook = BufferHook()
  var message = "foo"
  
  def init() {
    hook.add(message)
  }
}

object BufferTestFeature5 extends Feature("Buffer Test 5") {
  class Foo
  object Bar extends Foo
  val hook = BufferHook[Foo]("Test buffers 5", { foo: Foo => 
    foo match { case Bar => "foo" }
  })
  
  def init() {
    hook.add(Bar)
  }
}

object BufferTestFeature6 extends Feature("Buffer Test 6") {
  val hook = BufferHook()
  
  def init() {
    hook.add("foo")
    hook.earlyFilters.hook(fr => fr+"bar")
  }
}

object BufferTestFeature7 extends Feature("Buffer Test 7") {
  val hook = BufferHook()
  
  def init() {
    hook.add("foo")
    hook.lateFilters.hook(fr => fr+"bar")
  }
}

object BufferTestFeature8 extends Feature("Buffer Test 8") {
  val hook = BufferHook()
  val innerhook = BufferHook()
  
  def init() {
    hook.add("foo")
    hook.add(innerhook)
    hook.add("ged")
    innerhook.add("bar")
    innerhook.add("qux")
  }
}

object GuardTestFeature1 extends Feature("Guard Test 1") {
  val hook = GuardHook[String]()
  
  def init() {
  }
}

object GuardTestFeature2 extends Feature("Guard Test 2") {
  val hook = GuardHook[String]()
  
  def init() {
    hook.hook(m => m == "foo")
    hook.hook(m => m == "bar")
  }
}

