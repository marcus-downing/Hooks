package hooks

import scala.collection.mutable.{HashMap, ListBuffer}
import scala.util.DynamicVariable

object HookContext {
  def dummy() = new HookContextBuilderImpl(Nil)
  val contextVar = new DynamicVariable[HookContext](dummy())
  def get = contextVar.value
  def apply[R](f: (HookContext) => R): R = contextVar(f)
}

trait HookContext {
  def features: List[FeatureLike]
  def hasFeature(feature: FeatureLike) = features.contains(feature)
  def hasRegistered[S](hook: Hook[S]): Boolean
  def get[S](hook: Hook[S]): List[S]
  
  def using[R](f: => R): R = HookContext.contextVar.withValue(this)(f)
  
  //  temporary state
  def mutate: ContextBuilder = new HookContextMutant(new HookContextBuilderImpl(Nil), this)
}

object ContextBuilder {
  val builderVar = new DynamicVariable[ContextBuilder](null)
  def get = builderVar.value
  def apply[R](f: (ContextBuilder) => R): R = {
    if (builderVar.value == null) throw new ContextBuilderStateException("No mutable context builder active")
    builderVar(f)
  }
}

class ContextBuilderStateException(message: String) extends NullPointerException(message)

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
  
  def init(features: List[FeatureLike]) {
  
  }
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
}
