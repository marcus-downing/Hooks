package hooks

import scala.collection.mutable.{HashMap, ListBuffer}

trait PluginContext {
	def features: List[Feature]
	def plugins: List[Plugin]
	def hasFeature(feature: Feature) = features.contains(feature)
	def hasPlugin(plugin: Plugin) = plugins.contains(plugin)
	def hasRegistered[S](hook: Hook[S]): Boolean
	def get[S](hook: Hook[S]): List[S]
}

class PluginContextAdapter(inner: PluginContext) extends PluginContext {
	def features = inner.features
	def plugins = inner.plugins
	def hasRegistered[S](hook: Hook[S]) = inner.hasRegistered(hook)
	def get[S](hook: Hook[S]): List[S] = inner.get(hook)
}

class PluginContextBuilder (val features: List[Feature], val plugins: List[Plugin]) extends PluginContext {
	val registry: HashMap[Hook[_], ListBuffer[_]] = HashMap()
	def getValues[S](hook: Hook[S]) = registry.get(hook).getOrElse(ListBuffer()).asInstanceOf[ListBuffer[S]]
	
	def register[S](hook: Hook[S], value: S) {
		val list = getValues(hook)
		list += value
		registry(hook) = list
	}
	def hasRegistered[S](hook: Hook[S]) = !getValues(hook).isEmpty
	def get[S](hook: Hook[S]) = getValues(hook).toList
}

case class PluginContextImpl (
    features: List[Feature],
    plugins: List[Plugin],
    registry: Map[Hook[_], List[_]]
  ) extends PluginContext {
	def hasRegistered[S](hook: Hook[S]) = !registry.get(hook).getOrElse(List()).isEmpty
	def get[S](hook: Hook[S]) = registry.get(hook).getOrElse(List()).map(_.asInstanceOf[S])
}
