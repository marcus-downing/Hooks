package hooks

import scala.collection.mutable.{HashMap, ListBuffer}
import scala.util.DynamicVariable

object HookContext {
  def dummy() = new HookContextBuilderImpl(Nil)
  val contextVar = new DynamicVariable[HookContext](dummy())
  def apply[R](f: (HookContext) => R): R = contextVar(f)
}

trait HookContext {
  def features: List[FeatureLike]
  def hasFeature(feature: FeatureLike) = features.contains(feature)
  def hasRegistered[S](hook: Hook[S]): Boolean
  def get[S](hook: Hook[S]): List[S]
  
  def using[R](f: => R): R = HookContext.contextVar.withValue(this)(f)
  
  //  mutations: local-variant contexts
  /*
  //def stacked[R](f: => R) = HookContext.stack.using(this)(f)
  def mutate = new HookContextMutant(new HookContextBuilderImpl(Nil), this)
  
  def local[R](f: (ContextBuilder) => R) = f(mutate)
  
  //def localStacked[R](f: => R) = mutate.stacked(f)
  
  def local[R](f: (ContextBuilder) => Unit)(g: (HookContext) => R) = {
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
  
  def local[C, R](f: (ContextBuilder) => C)(g: (C) => (HookContext) => R) = {
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

object ContextBuilder {
  val builderVar = new DynamicVariable[ContextBuilder](HookContext.dummy)
  def apply[R](f: (ContextBuilder) => R): R = builderVar(f)
}

trait ContextBuilder extends HookContext {
  def register[S](hook: Hook[S], value: S): Unit
  override def using[R](f: => R): R = super.using { ContextBuilder.builderVar.withValue(this)(f) }
}

class HookContextAdaptor(inner: HookContext) extends HookContext {
  def features = inner.features
  def hasRegistered[S](hook: Hook[S]) = inner.hasRegistered(hook)
  def get[S](hook: Hook[S]): List[S] = inner.get(hook)
}

class HookContextBuilderImpl (val features: List[FeatureLike]) extends ContextBuilder {
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

case class HookContextImpl (
    features: List[FeatureLike],
    registry: Map[Hook[_], List[_]]
  ) extends HookContext {
  def hasRegistered[S](hook: Hook[S]) = !registry.get(hook).getOrElse(List()).isEmpty
  def get[S](hook: Hook[S]) = registry.get(hook).getOrElse(List()).map(_.asInstanceOf[S])
}

class HookContextMutant (mutation: HookContextBuilderImpl, base: HookContext) extends ContextBuilder {
  def features = mutation.features ::: base.features
  def register[S](hook: Hook[S], value: S) = mutation.register(hook, value)
  def hasRegistered[S](hook: Hook[S]) = mutation.hasRegistered(hook) || base.hasRegistered(hook)
  def get[S](hook: Hook[S]) = mutation.get(hook) ::: base.get(hook)
  def fix: HookContext = throw new UnsupportedOperationException
  /*
  def fix: HookContext = {
    val mutantRegistry: Map[Hook[_], List[_]] = mutation.registry.toMap.mapValues(_.toList)
    val combinedRegistry: Map[Hook[_], List[_]] = {
      ...
    }
    new HookContextImpl(features, combinedRegistry)
  }
  */
}
