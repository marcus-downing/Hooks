package hooks

import scala.Product

/**
 * Hooks
 *
 * These objects identify and provide access to the collected functions and objects
 * attached to them.
 */

class Hook[S](val name: String) {
	def get(implicit c: PluginContext) = c.get(this)
}

//  A hook that stores objects of a given type
object ComponentHook {
	def apply[T](name: String) = new ComponentHook[T](name)
}

class ComponentHook[T](name: String) extends Hook[T](name) {
	def register(t: T)(implicit c: PluginContextBuilder) = c.register(this, t)

	def apply()(implicit c: PluginContext) = get
}

//  A hook that selects just one object
abstract class SelectableHook[T](name: String)(selector: (List[T], PluginContext) => Option[T]) extends Hook[T](name) {
	def register(t: T)(implicit c: PluginContextBuilder) = c.register(this, t)

	def selectValue(values: List[T])(implicit c: PluginContext): Option[T]
	def select(implicit c: PluginContext) = selectValue(get)
	def apply()(implicit c: PluginContext) = select
}

//  A hook that fires an action
object ActionHook {
	def apply(name: String) = new SimpleActionHook(name)
	def simple(name: String) = new SimpleActionHook(name)
	def apply[S](name: String) = new ActionHook[S](name)
}

class SimpleActionHook(name: String) extends Hook[(PluginContext) => Unit](name) {
	def registerAction(f: (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
	def register(f: (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
	def register(f: () => Unit)(implicit c: PluginContextBuilder) = c.register(this, Adapter(f).act _)
	
	case class Adapter(f: () => Unit) { def act(c: PluginContext) = f() }
	
	def actions(implicit c: PluginContext) = get
	def apply()(implicit c: PluginContext) { for (action <- actions) action(c) }
}

class ActionHook[S](name: String) extends Hook[(S) => (PluginContext) => Unit](name) {
	def registerAction(f: (S) => (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
	def register(f: (S, PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, Adapter1(f).filter _)
	def register(f: (S) => Unit)(implicit c: PluginContextBuilder) = c.register(this, Adapter2(f).filter _)
	case class Adapter1(f: (S, PluginContext) => Unit) { def filter(s: S)(c: PluginContext) = f(s, c) }
	case class Adapter2(f: (S) => Unit) { def filter(s: S)(c: PluginContext) = f(s) }
	
	def actions(implicit c: PluginContext) = get
	def apply(s: S)(implicit c: PluginContext) { for (action <- actions) action(s)(c) }
}

//  A hook that transforms a value
object FilterHook {
	def apply[V](name: String) = new SimpleFilterHook[V](name)
	def apply[V, S](name: String) = new FilterHook[V, S](name)
}

class SimpleFilterHook[V](name: String) extends Hook[(V) => (PluginContext) => V](name) {
	def registerFilter(f: (V) => (PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, f)
	
	def register(f: (V, PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, Adapter1(f).filter _)
	def register(f: (V) => V)(implicit c: PluginContextBuilder) = c.register(this, Adapter2(f).filter _)
	
	case class Adapter1(f: (V, PluginContext) => V) { def filter(v: V)(c: PluginContext) = f(v, c) }
	case class Adapter2(f: (V) => V) { def filter(v: V)(c: PluginContext) = f(v) }
	
	def filters(implicit c: PluginContext) = get
	def apply(value: V)(implicit c: PluginContext): V =
		filters.foldLeft(value) {
			(value, filter) => filter(value)(c)
		}
}

class FilterHook[V, S](name: String) extends Hook[(V) => (S) => (PluginContext) => V](name) {
	def registerFilter(f: (V) => (S) => (PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, f)
	
	def register(f: (V, S, PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, Adapter1(f).filter _)
	def register(f: (V, S) => V)(implicit c: PluginContextBuilder) = c.register(this, Adapter2(f).filter _)
	//def register(f: (V, PluginContext) => V)(implicit c: PluginContextBuilder, d: DummyImplicit) = c.register(this, Adapter3(f).filter _)
	def register(f: (V) => V)(implicit c: PluginContextBuilder) = c.register(this, Adapter4(f).filter _)
	
	case class Adapter1(f: (V, S, PluginContext) =>V) { def filter(v: V)(s: S)(c: PluginContext) = f(v,s,c) }
	case class Adapter2(f: (V, S) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v,s) }
	//case class Adapter3(f: (V, PluginContext) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v,c) }
	case class Adapter4(f: (V) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v) }

	def filters(implicit c: PluginContext) = get
	def apply(value: V)(extra: S)(implicit c: PluginContext): V =
		filters.foldLeft(value) { 
			(value, filter) => filter(value)(extra)(c)
		}
}

/*
//  A hook that converts between two types
class LensHook[O, I](name: String) extends Hook[O => I](name) {
	class LensHookCounterpart extends Hook[I => O](name) {
		def encode = LensHook.this.decode
		def decode = LensHook.this.encode
		def counterpart = LensHook.this
	}
	val counterpart = new LensHookCounterpart

	def registerEncoder(enc: O => I)(implicit c: PluginContextBuilder) = c.register(this, enc)
	def registerDecoder(dec: I => O)(implicit c: PluginContextBuilder) = c.register(counterpart, dec)

	def encode(o: O)(implicit c: PluginContext): Option[I] = {
		val encoders = get
		...
	}
	
	def decode(i: I)(implicit c: PluginContext): Option[O] = {
		val decoders = counterpart.get
		...
	}
}*/
