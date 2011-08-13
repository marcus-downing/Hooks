package hooks

import scala.Product

/**
 * Hooks
 *
 * These objects identify and provide access to the collected functions and objects
 * attached to them.
 */

class Hook[S](val name: String, val id: Int = PluginRepository.uniqueId) {
	def get(implicit c: PluginContext) = c.get(this)
}

//  A hook that stores objects of a given type
class ComponentHook[T](name: String) extends Hook[T](name) {
	def register(t: T)(implicit c: PluginContextBuilder) = c.register(this, t)

	def apply()(implicit c: PluginContext) = get
}

//  A hook that selects just one object
abstract class SelectableHook[T](name: String) extends Hook[T](name) {
	def register(t: T)(implicit c: PluginContextBuilder) = c.register(this, t)

	def selectValue(values: List[T])(implicit c: PluginContext): Option[T]
	def select(implicit c: PluginContext) = selectValue(get)
	def apply()(implicit c: PluginContext) = select
}

//  A hook that fires an action
class ActionHook[S](name: String) extends Hook[(S) => (PluginContext) => Unit](name) {
	def register(f: (S) => (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
	//def register(f: S => Unit)(implicit c: PluginContextBuilder) = c.register(this, act(f)_)
	//def act(f: S => Unit)(s: S)(implicit c: PluginContext) { f(s) }

	def actions(implicit c: PluginContext) = get
	def apply(s: S)(implicit c: PluginContext) { for (action <- actions) action(s)(c) }
}

//  A hook that transforms a value
class FilterHook[V, S](name: String) extends Hook[(V) => (S) => (PluginContext) => V](name) {
	def register(f: (V) => (S) => (PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, f)
	def register(f: (V) => (S) => V)(implicit c: PluginContextBuilder, d: DummyImplicit) = c.register(this, Adapter1(f).filter _)
	def register(f: (V) => (PluginContext) => V)(implicit c: PluginContextBuilder, d1: DummyImplicit, d2: DummyImplicit) = c.register(this, Adapter2(f).filter _)
	def register(f: (V) => V)(implicit c: PluginContextBuilder, d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit) = c.register(this, Adapter3(f).filter _)
	
	case class Adapter1(f: (V) => (S) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v)(s) }
	case class Adapter2(f: (V) => (PluginContext) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v)(c) }
	case class Adapter3(f: (V) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v) }
	
	//def register(f: V => PluginContext => V)(implicit c: PluginContextBuilder) = c.register(this, f)
	//def register(f: V => V)(implicit c: PluginContextBuilder) = c.register(this, act(f)_)
	//def act(f: V => Unit)(v: V)(s: S)(implicit c: PluginContext) = f(v)

	def filters(implicit c: PluginContext) = get
	def apply(value: V)(extra: S)(implicit c: PluginContext): V =
		filters.foldLeft(value) { 
			//(value: V, filter: (V) => (PluginContext) => V) => filter(value)(c)
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
