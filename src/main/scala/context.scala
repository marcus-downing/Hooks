package hooks

import scala.collection.mutable._

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
	val requiredFeatures = SetBuffer[Feature]()
	def optionalFeatures = registeredFeatures.diff(requiredFeatures).toList
	def register(features: Feature*) { registeredFeatures.appendAll(features) }
	def require(features: Feature*) { requiredFeatures.appendAll(features) }
	
	def hasFeature(feature: Feature) = registeredFeatures.contains(feature)
	def isRequired(feature: Feature) = requiredFeatures.contains(feature)
	
	//  find all plugin dependencies
	def tracePlugins(head: List[Plugin], permit: Feature => Boolean) = {
		def trace(past: List[Plugin], present: List[Plugin]): List[Plugin] = {
			if (present.empty) past
			else {
				val future = for {
					plugin <- present
					p <- plugin.require
				} yield p
				val future2 = for {
					p <- future.unique
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
					if (!edges.empty) {
						val errors = edges.map(e => e._1.name+" and "+e._2.name)
						throw new Exception("Cyclical graph: unsatisfied dependencies between "+errors.mkString(", "))
					}
					Nil
				case _ => iterate(nowfree ::: free.tail, edges diff followedEdges, n :: sofar)
			}
		}
	
		val edges = {
			val edges1 = for { p <- plugins; b = plugin.before } yield (b, p)
			val edges2 = for { p <- plugins; a = plugin.after } yield (p, a)
			(edges1 ::: edges2).distinct
		}
		val freeNodes = for { n <- plugins; if !edges.exists(edge => edge._2 == n) } yield n
		
		iterate(freeNodes, edges, Nil)
	}

	//  create a context
	def makeContext(desiredFeatures: List[Feature], permit: Feature => Boolean = true) = {
		val features = (desiredFeatures.intersect(optionalFeatures) ::: requiredFeatures).filter(f => permit(f))
		val plugins = sortPlugins(tracePlugins(features, permit))
		new PluginContext(features, plugins)
	}
}

object PluginRepository extends PluginRepository {
	private var _nextid = 1
	def uniqueId = { val id = _nextId; _nextId = _nextId + 1; id }
}

object PluginContext {
	def makeContext(desiredFeatures: List[Feature], permit: Feature => Boolean = true) =
		PluginRepository.makeContext(desiredFeatures, permit)
}

class PluginContext (features: List[Feature], plugins: List[Plugin]) {
	def hasFeature(feature: Feature) = features.contains(feature)
	def hasPlugin(plugin: Plugin) = plugins.contains(plugin)
	
	private var _locked = false
	for (plugin <- plugins)
		plugin.init
	_locked = true
	def locked = _locked

	private val registry: HashMap[Hook[_], List[(Int, Any)]]
	def register[S](hook: Hook[S], value: S) = {
		if (locked) throw new Exception("Cannot register hooks on a locked context")
		val id = PluginRepository.uniqueId
		val list = registry(hook).getOrElse(List())
		registry(hook) = (id, value) :: list
		id
	}
	def unregister[S](hook: Hook[S], id: Int) {
		if (locked) throw new Exception("Cannot register hooks on a locked context")
		val list = registry(hook).getOrElse(List())
		registry(hook) = list.filter(_._1 != id)
	}
	def unregisterAll[S](hook: Hook[S]) {
		if (locked) throw new Exception("Cannot register hooks on a locked context")
		registry(hook) = List()
	}
	def hasRegistered[S](hook: Hook[S]) = !registry(hook).getOrElse(List()).empty
	def get[S](hook: Hook[S]) = registry(hook).getOrElse(List()).map(_._2.asInstanceOf[S])
}