package hooks.test

import org.scalatest.Spec

import hooks._

class RepoSpec extends Spec {
  describe("A dummy context") {
    it("should store hook") {
      HookContext.dummy().using {
        val hook = FilterHook[String]("Dummy test")
        hook.register(v => "bar")
        val result = hook("foo")
        println("RepoSpec #14: "+result)
        assert(result == "bar")
      }
    }
  }
  
	describe("A repository") {
		describe("when empty") {
			it ("should be empty") {
				val repo = FeatureRepository()
				assert(repo.isEmpty)
			}
		}
		
		describe("with a feature") {
			it("should have that feature") {
				val repo = FeatureRepository()
				repo.register(TestFeature)
				assert(repo.hasFeature(TestFeature))
			}

      describe("and a security guard") {
        it("should refuse a feature if it needs to") {
          val repo = FeatureRepository()
          repo.register(TestFeature)
          repo.securityGuard.register(feature => false)
          val context = repo.makeContext(List(TestFeature))
          assert(!context.hasFeature(TestFeature))
        }
        it("should refuse required features") {
          val repo = FeatureRepository()
          repo.require(TestFeature)
          repo.securityGuard.register(feature => false)
          val context = repo.makeContext(List(TestFeature))
          assert(!context.hasFeature(TestFeature))
        }
        it("should refuse dependencies") {
          val repo = FeatureRepository()
          repo.register(TestFeature2)
          repo.securityGuard.register( feature => feature match {
            case TestFeature2B => false
            case _ => true
          })
          val context = repo.makeContext(List(TestFeature2))
          assert(context.hasFeature(TestFeature2A) && !context.hasFeature(TestFeature2B))
        }
        it("should use a security token to decide") {
          val repo = FeatureRepository()
          repo.require(TestFeature2)
          repo.securityGuard.register((feature, token) => {
            assert(token == Some("foo"))
            true
          })
          val context = repo.makeContext(Nil, "foo")
          assert(context.hasFeature(TestFeature2A))
        }
      }
			
			describe("not required") {
				it("should produce a context with no features") {
					val repo = FeatureRepository()
					repo.register(TestFeature)
					val context = repo.makeContext(List())
					assert(!context.hasFeature(TestFeature))
				}
				
				it("should produce a context with the desired feature") {
					val repo = FeatureRepository()
					repo.register(TestFeature)
					val context = repo.makeContext(List(TestFeature))
					assert(context.hasFeature(TestFeature))
				}
				
				it("should produce a context with the desired feature only once") {
					//println(" -- ONLY ONCE --")
					val repo = FeatureRepository()
					repo.register(TestFeature)
					val context = repo.makeContext(List(TestFeature))
					val features = context.features.filter(p => p == TestFeature)
					assert(features.length == 1)
				}
			}
			
			describe("required") {
				it("should require that feature") {
					val repo = FeatureRepository()
					repo.register(TestFeature)
					repo.require(TestFeature)
					assert(repo.isRequired(TestFeature))
				}
				
				it ("should produce a context with the required feature") {
					val repo = FeatureRepository()
					repo.register(TestFeature)
					repo.require(TestFeature)
					val context = repo.makeContext(List())
					assert(context.hasFeature(TestFeature))
				}
				
				it("should produce a context with the desired feature only once") {
					//println(" -- ONLY ONCE --")
					val repo = FeatureRepository()
					repo.register(TestFeature)
					repo.require(TestFeature)
					val context = repo.makeContext(List(TestFeature))
					assert(context.features.length == 1)
				}
			}
		} // with a feature
		
		describe("with a requirement for two features") {
			it("should produce a context with both features") {
				//Logging.logging = true
				val repo = FeatureRepository()
				repo.register(TestFeature2)
				val context = repo.makeContext(List(TestFeature2))
				assert(context.hasFeature(TestFeature2A))
				assert(context.hasFeature(TestFeature2B))
			}
			
			describe("where A is before B") {
				it("should produce a context with both features in order") {
					val repo = FeatureRepository()
					repo.register(TestFeature3)
					val context = repo.makeContext(List(TestFeature3))
					assert(context.hasFeature(TestFeature3A))
					assert(context.hasFeature(TestFeature3B))
					assert(context.features.indexOf(TestFeature3A) < context.features.indexOf(TestFeature3B))
				}
			}
			
			describe("where B is before A") {
				it("should produce a context with both features in order") {
					val repo = FeatureRepository()
					repo.register(TestFeature4)
					val context = repo.makeContext(List(TestFeature4))
					assert(context.hasFeature(TestFeature4A))
					assert(context.hasFeature(TestFeature4B))
					assert(context.features.indexOf(TestFeature4A) < context.features.indexOf(TestFeature4B))
				}
			}
			
			describe("where the order is unsolvable") {
				it("should throw a dependency exception") {
					val repo = FeatureRepository()
					repo.register(TestFeature5)
          intercept[FeatureDependencyException] { repo.makeContext(List(TestFeature5)) }
				}
			}
			
			describe("with outside dependencies") {
				it("should exclude them") {
					val repo = FeatureRepository()
					repo.register(TestFeature6)
					val context = repo.makeContext(List(TestFeature6))
					assert(!context.hasFeature(TestFeature5A))
				}
			}
		} // two features
		
		describe("with a circular dependency between three features") {
			it("should contain all three features") {
				val repo = FeatureRepository()
				repo.register(TestFeature7)
				val context = repo.makeContext(List(TestFeature7))
				assert(context.hasFeature(TestFeature7) && context.hasFeature(TestFeature7A) && context.hasFeature(TestFeature7B))
			}
			it("should contain exactly three features") {
				val repo = FeatureRepository()
				repo.register(TestFeature7)
				val context = repo.makeContext(List(TestFeature7))
				assert(context.features.length == 3)
			}
		}
	} // repo
}

// test 1: features

object TestFeature extends Feature("Test Feature 1") {
	def init() { }
}

// test 2: dependencies

object TestFeature2 extends Feature("Test Feature 2", depend = List(TestFeature2A, TestFeature2B)) {
	def init() { }
}

object TestFeature2A extends FeatureLike("Test Feature 2A") {
	def init() { }
}

object TestFeature2B extends FeatureLike("Test Feature 2B") {
	def init() { }
}

// test 3: before

object TestFeature3 extends Feature("Test Feature 3", depend = List(TestFeature3A, TestFeature3B)) {
	def init() { }
}

object TestFeature3A extends FeatureLike("Test Feature 3A", before = List(TestFeature3B)) {
	def init() { }
}

object TestFeature3B extends FeatureLike("Test Feature 3B") {
	def init() { }
}

// test 4: after

object TestFeature4 extends Feature("Test Feature 4", depend = List(TestFeature4A, TestFeature4B)) {
	def init() { }
}

object TestFeature4A extends FeatureLike("Test Feature 4A") {
	def init() { }
}

object TestFeature4B extends FeatureLike("Test Feature 4B", after = List(TestFeature4A)) {
	def init() { }
}

// test 5: incompatible list

object TestFeature5 extends Feature("Test Feature 5", depend = List(TestFeature5A, TestFeature5B)) {
	def init() { }
}

object TestFeature5A extends FeatureLike("Test Feature 5A", after = List(TestFeature5B)) {
	def init() { }
}

object TestFeature5B extends FeatureLike("Test Feature 5B", after = List(TestFeature5A)) {
	def init() { }
}

// test 6: outside dependencies

object TestFeature6 extends Feature("Test Feature 6", depend = List(TestFeature6A)) {
	def init() { }
}

object TestFeature6A extends FeatureLike("Test Feature 6", after = List(TestFeature5A)) {
	def init() { }
}

//  test 7: circular dependencies

object TestFeature7 extends Feature("Test Feature 7", depend = List(TestFeature7A)) {
	def init() { }
}

object TestFeature7A extends FeatureLike("Test Feature 7A", depend = List(TestFeature7B)) {
	def init() { }
}

object TestFeature7B extends FeatureLike("Test Feature 7B", depend = List(TestFeature7)) {
	def init() { }
}
