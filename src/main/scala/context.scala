package hooks

import scala.collection.mutable.{HashMap, ListBuffer}

object PluginContext {
  def dummy = new PluginContextBuilderImpl(Nil, Nil)
  val stack = new ResourceTracker[PluginContext, String]() // .sync
  //implicit def stackContext = stack.get[PluginContext].getOrElse(dummy)
  //implicit def stackBuilder = stack.get[PluginContextBuilder].getOrElse(dummy)
}

trait PluginContext {
  def features: List[Feature]
  def plugins: List[Plugin]
  def hasFeature(feature: Feature) = features.contains(feature)
  def hasPlugin(plugin: Plugin) = plugins.contains(plugin)
  def hasRegistered[S](hook: Hook[S]): Boolean
  def get[S](hook: Hook[S]): List[S]
  
  //  mutations: local-variant contexts
  //def stacked[R](f: => R) = PluginContext.stack.using(this)(f)
  def mutate = new PluginContextMutant(new PluginContextBuilderImpl(Nil, Nil), this)
  
  def local[R](f: (PluginContextBuilder) => R) = f(mutate)
  //def localStacked[R](f: => R) = mutate.stacked(f)
  /*
  def local[R](f: (PluginContextBuilder) => Unit)(g: (PluginContext) => R) = {
    val mutant = mutate
    f(mutant)
    val fixed = mutant.fix
    g(fixed)
  }
  def localStacked[R](f: => Unit)(g: => R) = {
    val mutant = mutate
    mutant.stacked(f)
    val fixed = mutant.fix
    fixed.stacked(g)
  }
  */
  /*
  def local[C, R](f: (PluginContextBuilder) => C)(g: (C) => (PluginContext) => R) = {
    val mutant = mutate
    val carry: C = f(mutant)
    val fixed = mutant.fix
    g(carry)(fixed)
  }
  def localStacked[C, R](f: => C)(g: (C) => R) = {
    val mutant = mutate
    val carry: C = mutant.stacked(f)
    val fixed = mutant.fix
    fixed.stacked(g(carry))
  }
  */
}

trait PluginContextBuilder extends PluginContext {
  def register[S](hook: Hook[S], value: S)
}

class PluginContextAdaptor(inner: PluginContext) extends PluginContext {
  def features = inner.features
  def plugins = inner.plugins
  def hasRegistered[S](hook: Hook[S]) = inner.hasRegistered(hook)
  def get[S](hook: Hook[S]): List[S] = inner.get(hook)
}

class PluginContextBuilderImpl (val features: List[Feature], val plugins: List[Plugin]) extends PluginContextBuilder {
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

class PluginContextMutant (mutation: PluginContextBuilderImpl, base: PluginContext) extends PluginContextBuilder {
  def features = mutation.features ::: base.features
  def plugins = mutation.plugins ::: base.plugins
  def register[S](hook: Hook[S], value: S) = mutation.register(hook, value)
  def hasRegistered[S](hook: Hook[S]) = mutation.hasRegistered(hook) || base.hasRegistered(hook)
  def get[S](hook: Hook[S]) = mutation.get(hook) ::: base.get(hook)
  def fix: PluginContext = throw new UnsupportedOperationException
  /*
  def fix: PluginContext = {
    val mutantRegistry: Map[Hook[_], List[_]] = mutation.registry.toMap.mapValues(_.toList)
    val combinedRegistry: Map[Hook[_], List[_]] = {
      ...
    }
    new PluginContextImpl(features, plugins, combinedRegistry)
  }
  */
}