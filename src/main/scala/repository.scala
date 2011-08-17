package hooks

import scala.collection.mutable.{HashMap, ListBuffer}

/**
 * Plugin Repository and Context
 *
 * A repository is a store of all the potential features registered.
 * You can use either the PluginRepository object, or create an instance.
 *
 * A context is a collection of activated features, and the hooks they've initialised.
 * The context you create should be passed around as an implicit value.
 */

object Logging {
	var logging = false
}
 
trait Logging {
	def log(f: => String) {
		if (Logging.logging) println(f)
	}
	implicit def richPluginList(plugins: List[Plugin]) = new { def l = plugins.map(_.name).mkString(", ") }
	implicit def richEdgeList(edges: List[(Plugin, Plugin)]) = new { def l = edges.map(e => e._1.name+" -> "+e._2.name) }
}
 
trait PluginRepository {
	def features: List[Feature]
	def requiredFeatures: List[Feature]
	def optionalFeatures: List[Feature] = features.diff(requiredFeatures)
	
	def register(features: Feature*)
	def require(features: Feature*)
	
	def isEmpty = features.isEmpty
	def hasFeature(feature: Feature) = features.contains(feature)
	def hasFeatures(features: Feature*) = features.forall(f => hasFeature(f))
	def isRequired(features: Feature*) = features.forall(f => requiredFeatures.contains(f))
	
	def makeContext(desiredFeatures: List[Feature]): PluginContext
	def makeContext(desiredFeatures: List[Feature], token: Any): PluginContext
	
	def copy = {
		val copy = new PluginRepositoryImpl
		copy.register(this.features: _*)
		copy.require(this.requiredFeatures: _*)
		copy
	}
}

class PluginRepositoryImpl extends PluginRepository with Logging {
	val _registeredFeatures = ListBuffer[Feature]()
	val _requiredFeatures = ListBuffer[Feature]()
	def features = _registeredFeatures.toList
	def requiredFeatures = _requiredFeatures.toList

	def register(features: Feature*) {
	  _registeredFeatures.appendAll(features.diff(_registeredFeatures))
	}
	def require(features: Feature*) {
	  register(features: _*)
	  _requiredFeatures.appendAll(features.diff(_requiredFeatures))
	}
	
	//def isEmpty: Boolean = registeredFeatures.isEmpty
	//def hasFeature(feature: Feature) = registeredFeatures.contains(feature)
	//def isRequired(feature: Feature) = requiredFeatures.contains(feature)
	
	val securityGuard = GuardHook.standalone[Plugin, Option[Any]]("Repository guard")
	
	//  find all plugin dependencies
	def tracePlugins(head: List[Plugin], token: Option[Any]) = {
		log("TRACE: "+head.l)
		def trace(past: List[Plugin], present: List[Plugin]): List[Plugin] = {
			log("  Tracing: "+present.l+" / So far: "+past.l)
			if (present.isEmpty) past
			else {
				present.foreach { p =>
					log("    Plugin: "+p.name)
					log("      Depends on: "+p.depend.l)
				}
				val future = for {
					plugin <- present
					p <- plugin.depend
				} yield p
				log("  Future 1: "+future.l);
				val future2 = future.distinct.filter { p =>
					log("    Testing: "+p.name)
					if (past.contains(p)) log("      In past")
					if (present.contains(p)) log("     In present")
					if (!securityGuard(p)(token)) log("      Not permitted")
					!past.contains(p) && !present.contains(p) && securityGuard(p)(token)
				}
				log("  Future 2: "+future2.l)
				trace(present ::: past, future2)
			}
		}
		val t = trace(Nil, head)
		log("DONE: "+t.map(_.name).mkString(", "))
		t
	}
	
	//  topological sort by the Kuhn algorithm
	def sortPlugins(plugins: List[Plugin]) = {
		log("SORT: "+plugins.l)
		def iterate(free: List[Plugin], edges: List[(Plugin, Plugin)], sofar: List[Plugin]): List[Plugin] = {
			log("    Free: "+free.l+" / So far: "+sofar.l)
			if (sofar.length > plugins.length) { println("So far: "+sofar.l); throw new Exception("So far: "+sofar.l+"; Plugins: "+plugins.l) }
			free match {
				case Nil =>
					if (!edges.isEmpty) throw new PluginDependencyException(edges)
					log("    Done");
					sofar
				case head :: tail =>
					log("      Head: "+head.name+" / Tail: "+tail.map(_.name).mkString(", "))
					val followedEdges = for {
						e <- edges; if e._1 == head
						m = e._2; if !edges.exists(e2 => e != e2 && e2._2 == m)
					} yield e
					log("      Followed edges: "+followedEdges.map(e => e._1.name+" -> "+e._2.name).mkString(", "))
					val newlyfree = followedEdges.map(_._2)
					log("      Newly free: "+newlyfree.map(_.name).mkString(", "))
					val nowfree = (newlyfree ::: free.tail).distinct
					log("      Now free: "+nowfree.map(_.name).mkString(", "))
					iterate(nowfree, edges diff followedEdges, head :: sofar)
			}
		}
	
		log("  Edges...")
		val edges: List[(Plugin, Plugin)] = {
			val edges1: List[(Plugin, Plugin)] = for { p <- plugins; b <- p.before.intersect(plugins) } yield (b, p)
			val edges2: List[(Plugin, Plugin)] = for { p <- plugins; a <- p.after.intersect(plugins) } yield (p, a)
			(edges1 ::: edges2).distinct
		}
		log("  Edges: "+edges.map( e => e._1.name+" -> "+e._2.name).mkString(", "))
		val freeNodes = for { n <- plugins; if !edges.exists(edge => edge._2 == n) } yield n
		log("  Free nodes: "+freeNodes.map(_.name).mkString(", "))
		
		val s = iterate(freeNodes, edges, Nil)
		log("DONE: "+s.map(_.name).mkString(", "))
		s
	}

	//  create a context
	def makeContext(desiredFeatures: List[Feature]): PluginContext = makeContext(desiredFeatures, None)
	def makeContext(desiredFeatures: List[Feature], token: Any): PluginContext = makeContext(desiredFeatures, Some(token))

	def makeContext(desiredFeatures: List[Feature], token: Option[Any]): PluginContext = {
		val features = (desiredFeatures.intersect(optionalFeatures) ::: requiredFeatures.toList).filter(f => securityGuard(f)(token))
		val plugins = sortPlugins(tracePlugins(features, token))
		
		//  initialise all the plugins in order
		val builder = new PluginContextBuilder(features, plugins)
		for (plugin <- plugins)
			plugin.init(builder)
		val registry: Map[Hook[_], List[_]] = 
			builder.registry.toMap.mapValues(_.toList)

		new PluginContextImpl(features, plugins, registry)
	}
}

class SynchronizedPluginRepository(val inner: PluginRepositoryImpl) extends PluginRepository {
	def features = inner.synchronized { inner.features }
	def requiredFeatures = inner.synchronized { inner.requiredFeatures }
	
	def register(features: Feature*) { inner.synchronized { inner.register(features: _*) } }
	def require(features: Feature*) { inner.synchronized { inner.require(features: _*) } }
	
	def makeContext(desiredFeatures: List[Feature]) =
		inner.synchronized { inner.makeContext(desiredFeatures) }
	def makeContext(desiredFeatures: List[Feature], token: Any) =
		inner.synchronized { inner.makeContext(desiredFeatures, token) }
}

object PluginRepository extends PluginRepository {
	val instance = new SynchronizedPluginRepository(new PluginRepositoryImpl)
	def apply() = new PluginRepositoryImpl
	
	def features = instance.features
	def requiredFeatures = instance.requiredFeatures
	
	def register(features: Feature*) { instance.register(features: _*) }
	def require(features: Feature*) { instance.require(features: _*) }
	
	def securityGuard = instance.inner.securityGuard
	def makeContext(desiredFeatures: List[Feature]) = instance.makeContext(desiredFeatures)
	def makeContext(desiredFeatures: List[Feature], token: Any) = instance.makeContext(desiredFeatures, token)
}
