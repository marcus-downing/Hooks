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

class PluginRepository {
	val registeredFeatures = ListBuffer[Feature]()
	val requiredFeatures = ListBuffer[Feature]()
	def optionalFeatures = registeredFeatures.diff(requiredFeatures).toList
	def register(features: Feature*) { registeredFeatures.appendAll(features) }
	def require(features: Feature*) { requiredFeatures.appendAll(features) }
	
	def hasFeature(feature: Feature) = registeredFeatures.contains(feature)
	def isRequired(feature: Feature) = requiredFeatures.contains(feature)
	
	//  find all plugin dependencies
	def tracePlugins(head: List[Plugin], permit: Plugin => Boolean) = {
		def trace(past: List[Plugin], present: List[Plugin]): List[Plugin] = {
			if (present.isEmpty) past
			else {
				val future = for {
					plugin <- present
					p <- plugin.require
				} yield p
				val future2 = for {
					p <- future.distinct
					if !past.contains(p) && !present.contains(p)
					if permit(p)
				} yield p
				trace(present ::: past, future2)
			}
		}
		trace(Nil, head)
	}
	
	//  topological sort by the Kuhn algorithm
	def sortPlugins(plugins: List[Plugin]) = {
		def iterate(free: List[Plugin], edges: List[(Plugin, Plugin)], sofar: List[Plugin]): List[Plugin] = {
			val n = free.head
			val followedEdges = for {
				e <- edges; if e._1 == n
				m = e._2; if !edges.exists(e2 => e != e2 && e2._2 == m)
			} yield e
			val newlyfree = followedEdges.map(_._2)
			val nowfree = (newlyfree ::: free.tail).distinct
			nowfree match {
				case Nil => 
					if (!edges.isEmpty) {
						val errors = edges.map(e => e._1.name+" and "+e._2.name)
						throw new Exception("Cyclical graph: unsatisfied dependencies between "+errors.mkString(", "))
					}
					Nil
				case _ => iterate(nowfree ::: free.tail, edges diff followedEdges, n :: sofar)
			}
		}
	
		val edges: List[(Plugin, Plugin)] = {
			val edges1: List[(Plugin, Plugin)] = for { p <- plugins; b <- p.before } yield (b, p)
			val edges2: List[(Plugin, Plugin)] = for { p <- plugins; a <- p.after } yield (p, a)
			(edges1 ::: edges2).distinct
		}
		val freeNodes = for { n <- plugins; if !edges.exists(edge => edge._2 == n) } yield n
		
		iterate(freeNodes, edges, Nil)
	}

	//  create a context
	def makeContext(desiredFeatures: List[Feature], permit: Plugin => Boolean = p => true) = {
		val features = (desiredFeatures.intersect(optionalFeatures) ::: requiredFeatures.toList).filter(f => permit(f))
		val plugins = sortPlugins(tracePlugins(features, permit))
		
		//  initialise all the plugins in order
		val builder = new PluginContextBuilder(features, plugins)
		for (plugin <- plugins)
			plugin.init(builder)
		val registry: Map[Hook[_], List[_]] = builder.registry.toMap

		new PluginContextImpl(features, plugins, registry)
	}
}

object PluginRepository extends PluginRepository {
	private var _nextId = 1
	def uniqueId = { val id = _nextId; _nextId = _nextId + 1; id }
}

trait PluginContext {
	def hasFeature(feature: Feature):Boolean
	def hasPlugin(plugin: Plugin): Boolean
	def hasRegistered[S](hook: Hook[S]): Boolean
	def get[S](hook: Hook[S]): List[S]
}

class PluginContextBuilder (features: List[Feature], plugins: List[Plugin]) extends PluginContext {
	def hasFeature(feature: Feature) = features.contains(feature)
	def hasPlugin(plugin: Plugin) = plugins.contains(plugin)

	val registry = HashMap[Hook[_], List[_]]()
	def getValues[S](hook: Hook[S]) = registry.get(hook).getOrElse(List()).asInstanceOf[List[S]]
	
	def register[S](hook: Hook[S], value: S) {
		registry(hook) = value :: getValues(hook)
	}
	def hasRegistered[S](hook: Hook[S]) = !getValues(hook).isEmpty
	def get[S](hook: Hook[S]) = getValues(hook)
}

case class PluginContextImpl (features: List[Feature], plugins: List[Plugin], registry: Map[Hook[_], List[_]]) extends PluginContext {
	def hasFeature(feature: Feature) = features.contains(feature)
	def hasPlugin(plugin: Plugin) = plugins.contains(plugin)

	def hasRegistered[S](hook: Hook[S]) = !registry.get(hook).getOrElse(List()).isEmpty
	def get[S](hook: Hook[S]) = registry.get(hook).getOrElse(List()).map(_.asInstanceOf[S])
}
