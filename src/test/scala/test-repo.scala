package hooks.test

import org.scalatest.Spec

import hooks._

class RepoSpec extends Spec {
  describe("A dummy context") {
    it("should store hook") {
      implicit val dummy = PluginContext.dummy
      val hook = FilterHook[String]("Dummy test")
      hook.register(v => "bar")
      val result = hook("foo")
      assert(result == "bar")
    }
  }
  
	describe("A repository") {
		describe("when empty") {
			it ("should be empty") {
				val repo = PluginRepository()
				assert(repo.isEmpty)
			}
		}
		
		describe("with a feature") {
			it("should have that feature") {
				val repo = PluginRepository()
				repo.register(TestFeature)
				assert(repo.hasFeature(TestFeature))
			}

      describe("and a security guard") {
        it("should refuse a feature if it needs to") {
          val repo = PluginRepository()
          repo.register(TestFeature)
          repo.securityGuard.register(plugin => false)
          val context = repo.makeContext(List(TestFeature))
          assert(!context.hasFeature(TestFeature))
        }
        it("should refuse required features") {
          val repo = PluginRepository()
          repo.require(TestFeature)
          repo.securityGuard.register(plugin => false)
          val context = repo.makeContext(List(TestFeature))
          assert(!context.hasFeature(TestFeature))
        }
        it("should refuse dependencies") {
          val repo = PluginRepository()
          repo.register(TestFeature2)
          repo.securityGuard.register( plugin => plugin match {
            case TestPlugin2B => false
            case _ => true
          })
          val context = repo.makeContext(List(TestFeature2))
          assert(context.hasPlugin(TestPlugin2A) && !context.hasPlugin(TestPlugin2B))
        }
        it("should use a security token to decide") {
          val repo = PluginRepository()
          repo.require(TestFeature2)
          repo.securityGuard.register((plugin, token) => {
            assert(token == Some("foo"))
            true
          })
          val context = repo.makeContext(Nil, "foo")
          assert(context.hasPlugin(TestPlugin2A))
        }
      }
			
			describe("not required") {
				it("should produce a context with no features") {
					val repo = PluginRepository()
					repo.register(TestFeature)
					val context = repo.makeContext(List())
					assert(!context.hasFeature(TestFeature))
				}
				
				it("should produce a context with the desired feature") {
					val repo = PluginRepository()
					repo.register(TestFeature)
					val context = repo.makeContext(List(TestFeature))
					assert(context.hasFeature(TestFeature))
				}
				
				it("should produce a context with the desired feature only once") {
					//println(" -- ONLY ONCE --")
					val repo = PluginRepository()
					repo.register(TestFeature)
					val context = repo.makeContext(List(TestFeature))
					val plugins = context.plugins.filter(p => p == TestFeature)
					//println(" -- RESULT: "+context.plugins.map(_.name).mkString(", ")+" --")
					assert(plugins.length == 1)
				}
			}
			
			describe("required") {
				it("should require that feature") {
					val repo = PluginRepository()
					repo.register(TestFeature)
					repo.require(TestFeature)
					assert(repo.isRequired(TestFeature))
				}
				
				it ("should produce a context with the required feature") {
					val repo = PluginRepository()
					repo.register(TestFeature)
					repo.require(TestFeature)
					val context = repo.makeContext(List())
					assert(context.hasFeature(TestFeature))
				}
				
				it("should produce a context with the desired feature only once") {
					//println(" -- ONLY ONCE --")
					val repo = PluginRepository()
					repo.register(TestFeature)
					repo.require(TestFeature)
					val context = repo.makeContext(List(TestFeature))
					val plugins = context.plugins
					//println(" -- RESULT: "+context.plugins.map(_.name).mkString(", ")+" --")
					assert(plugins.length == 1)
				}
			}
		} // with a feature
		
		describe("with a requirement for two plugins") {
			it("should produce a context with both plugins") {
				//Logging.logging = true
				val repo = PluginRepository()
				repo.register(TestFeature2)
				val context = repo.makeContext(List(TestFeature2))
				assert(context.hasPlugin(TestPlugin2A))
				assert(context.hasPlugin(TestPlugin2B))
				//Logging.logging = false
			}
			
			describe("where A is before B") {
				it("should produce a context with both plugins in order") {
					val repo = PluginRepository()
					repo.register(TestFeature3)
					val context = repo.makeContext(List(TestFeature3))
					assert(context.hasPlugin(TestPlugin3A))
					assert(context.hasPlugin(TestPlugin3B))
					assert(context.plugins.indexOf(TestPlugin3A) < context.plugins.indexOf(TestPlugin3B))
				}
			}
			
			describe("where B is before A") {
				it("should produce a context with both plugins in order") {
					val repo = PluginRepository()
					repo.register(TestFeature4)
					val context = repo.makeContext(List(TestFeature4))
					assert(context.hasPlugin(TestPlugin4A))
					assert(context.hasPlugin(TestPlugin4B))
					assert(context.plugins.indexOf(TestPlugin4A) < context.plugins.indexOf(TestPlugin4B))
				}
			}
			
			describe("where the order is unsolvable") {
				it("should throw a dependency exception") {
					val repo = PluginRepository()
					repo.register(TestFeature5)
					intercept[PluginDependencyException] { repo.makeContext(List(TestFeature5)) }
				}
			}
			
			describe("with outside dependencies") {
				it("should exclude them") {
					val repo = PluginRepository()
					repo.register(TestFeature6)
					val context = repo.makeContext(List(TestFeature6))
					assert(!context.hasPlugin(TestPlugin5A))
				}
			}
		} // two plugins
		
		describe("with a circular dependency between three plugins") {
			it("should contain all three plugins") {
				val repo = PluginRepository()
				repo.register(TestFeature7)
				val context = repo.makeContext(List(TestFeature7))
				assert(context.hasPlugin(TestFeature7) && context.hasPlugin(TestPlugin7A) && context.hasPlugin(TestPlugin7B))
			}
			it("should contain exactly three plugins") {
				val repo = PluginRepository()
				repo.register(TestFeature7)
				val context = repo.makeContext(List(TestFeature7))
				assert(context.plugins.length == 3)
			}
		}
	} // repo
}

// test 1: features

object TestFeature extends Feature {
	def name = "Test Feature"
	override def depend = Nil
	def init(implicit builder: PluginContextBuilder) { }
}

// test 2: dependencies

object TestFeature2 extends Feature {
	def name = "Test Feature 2"
	override def depend = List(TestPlugin2A, TestPlugin2B)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin2A extends Plugin {
	def name = "Test Plugin 2A"
	override def depend = Nil
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin2B extends Plugin {
	def name = "Test Plugin 2B"
	override def depend = Nil
	def init(implicit builder: PluginContextBuilder) { }
}

// test 3: before

object TestFeature3 extends Feature {
	def name = "Test Feature 3"
	override def depend = List(TestPlugin3A, TestPlugin3B)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin3A extends Plugin {
	def name = "Test Plugin 3A"
	override def depend = Nil
	override def before = List(TestPlugin3B)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin3B extends Plugin {
	def name = "Test Plugin 3B"
	override def depend = Nil
	def init(implicit builder: PluginContextBuilder) { }
}

// test 4: after

object TestFeature4 extends Feature {
	def name = "Test Feature 4"
	override def depend = List(TestPlugin4A, TestPlugin4B)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin4A extends Plugin {
	def name = "Test Plugin 4A"
	override def depend = Nil
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin4B extends Plugin {
	def name = "Test Plugin 4B"
	override def depend = Nil
	override def after = List(TestPlugin4A)
	def init(implicit builder: PluginContextBuilder) { }
}

// test 5: incompatible list

object TestFeature5 extends Feature {
	def name = "Test Feature 5"
	override def depend = List(TestPlugin5A, TestPlugin5B)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin5A extends Plugin {
	def name = "Test Plugin 5A"
	override def depend = Nil
	override def after = List(TestPlugin5B)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin5B extends Plugin {
	def name = "Test Plugin 5B"
	override def depend = Nil
	override def after = List(TestPlugin5A)
	def init(implicit builder: PluginContextBuilder) { }
}

// test 6: outside dependencies

object TestFeature6 extends Feature {
	def name = "Test Feature 6"
	override def depend = List(TestPlugin6)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin6 extends Plugin {
	def name = "Test Plugin 6"
	override def depend = Nil
	override def after = List(TestPlugin5A)
	def init(implicit builder: PluginContextBuilder) { }
}

//  test 7: circular dependencies

object TestFeature7 extends Feature {
	def name = "Test Feature 7"
	override def depend = List(TestPlugin7A)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin7A extends Plugin {
	def name = "Test Plugin 7A"
	override def depend = List(TestPlugin7B)
	def init(implicit builder: PluginContextBuilder) { }
}

object TestPlugin7B extends Plugin {
	def name = "Test Plugin 7B"
	override def depend = List(TestFeature7)
	def init(implicit builder: PluginContextBuilder) { }
}
