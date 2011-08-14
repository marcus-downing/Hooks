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
	def registeredFeatures: Seq[Feature]
	def requiredFeatures: Seq[Feature]
	def optionalFeatures: Seq[Feature]
	
	def register(features: Feature*)
	def require(features: Feature*)
	
	def isEmpty: Boolean
	def hasFeature(feature: Feature): Boolean
	def isRequired(feature: Feature): Boolean
	
	def makeContext(desiredFeatures: List[Feature]): PluginContext = makeContext(desiredFeatures, p => true)
	def makeContext(desiredFeatures: List[Feature], permit: Plugin => Boolean): PluginContext
	
	def copy = {
		val copy = new PluginRepositoryImpl
		copy.require(this.requiredFeatures: _*)
		copy.register(this.registeredFeatures: _*)
		copy
	}
}

class PluginRepositoryImpl extends PluginRepository with Logging {
	val registeredFeatures = ListBuffer[Feature]()
	val requiredFeatures = ListBuffer[Feature]()
	def optionalFeatures = registeredFeatures.diff(requiredFeatures).toList
	def register(features: Feature*) { registeredFeatures.appendAll(features) }
	def require(features: Feature*) { requiredFeatures.appendAll(features) }
	
	def isEmpty: Boolean = registeredFeatures.isEmpty
	def hasFeature(feature: Feature) = registeredFeatures.contains(feature)
	def isRequired(feature: Feature) = requiredFeatures.contains(feature)
	
	//  find all plugin dependencies
	def tracePlugins(head: List[Plugin], permit: Plugin => Boolean) = {
		log("TRACE: "+head.l)
		def trace(past: List[Plugin], present: List[Plugin]): List[Plugin] = {
			log("  Tracing: "+present.l+" / So far: "+past.l)
			if (present.isEmpty) past
			else {
				present.foreach { p =>
					log("    Plugin: "+p.name)
					log("      Requires: "+p.require.l)
				}
				val future = for {
					plugin <- present
					p <- plugin.require
				} yield p
				log("  Future 1: "+future.l);
				val future2 = future.distinct.filter { p =>
					log("    Testing: "+p.name)
					if (past.contains(p)) log("      In past")
					if (present.contains(p)) log("     In present")
					if (!permit(p)) log("      Not permitted")
					!past.contains(p) && !present.contains(p) && permit(p)
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
	def makeContext(desiredFeatures: List[Feature], permit: Plugin => Boolean = p => true) = {
		val features = (desiredFeatures.intersect(optionalFeatures) ::: requiredFeatures.toList).filter(f => permit(f))
		val plugins = sortPlugins(tracePlugins(features, permit))
		
		//  initialise all the plugins in order
		val builder = new PluginContextBuilder(features, plugins)
		for (plugin <- plugins)
			plugin.init(builder)
		val registry: Map[Hook[_], List[_]] = 
			builder.registry.toMap.mapValues(_.toList)

		new PluginContextImpl(features, plugins, registry)
	}
}

class SynchronizedPluginRepository(inner: PluginRepositoryImpl) extends PluginRepository {
	def registeredFeatures: Seq[Feature] = inner.synchronized { inner.registeredFeatures }
	def requiredFeatures: Seq[Feature] = inner.synchronized { inner.requiredFeatures }
	def optionalFeatures: Seq[Feature] = inner.synchronized { inner.optionalFeatures }
	
	def register(features: Feature*) { inner.synchronized { inner.register(features: _*) } }
	def require(features: Feature*) { inner.synchronized { inner.require(features: _*) } }
	
	def isEmpty = inner.synchronized { inner.isEmpty }
	def hasFeature(feature: Feature) = inner.synchronized { inner.hasFeature(feature) }
	def isRequired(feature: Feature) = inner.synchronized { inner.isRequired(feature) }
	
	def makeContext(desiredFeatures: List[Feature], permit: Plugin => Boolean) =
		inner.synchronized { inner.makeContext(desiredFeatures, permit) }
}

object PluginRepository extends PluginRepository {
	val instance = new SynchronizedPluginRepository(new PluginRepositoryImpl)
	def apply() = new PluginRepositoryImpl
	
	def registeredFeatures = instance.registeredFeatures
	def requiredFeatures = instance.requiredFeatures
	def optionalFeatures = instance.optionalFeatures
	
	def register(features: Feature*) { instance.register(features: _*) }
	def require(features: Feature*) { instance.require(features: _*) }
	
	def isEmpty = instance.isEmpty
	def hasFeature(feature: Feature) = instance.hasFeature(feature)
	def isRequired(feature: Feature) = instance.isRequired(feature)
	
	def makeContext(desiredFeatures: List[Feature], permit: Plugin => Boolean) = instance.makeContext(desiredFeatures, permit)
}