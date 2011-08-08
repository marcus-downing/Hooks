package hooks

import scala.Product

/**
 * Hooks
 *
 * These objects identify and provide access to the collected functions and objects
 * attached to them.
 */

class Hook[S](val name: String, val id = PluginContext.uniqueId) {
	def get(implicit c: PluginContext) = c.get(this)
	def unregister(id: Int)(implicit c: PluginContext) = c.unregister(this, id)
	def unregisterAll(implicit c: PluginContext) = c.unregisterAll(this)
}

//  A hook that stores objects of a given type
class ComponentHook[T](name: String) extends Hook[T](name) {
	def register(t: T)(implicit c: PluginContext) = c.register(this, t)
	def apply()(implicit c: PluginContext) = get
}

//  A hook that selects just one object
class SelectableHook[T](name: String) extends Hook[T](name) {
	def register(t: T)(implicit c: PluginContext) = c.register(this, t)
	def selectValue(values: List[T])(implicit c: PluginContext): Option[T]
	def select(implicit c: PluginContext) = selectValue(get)
	def apply()(implicit c: PluginContext) = select
}

//  A hook that fires an action
class ActionHook[S <: Product](name: String) extends Hook[S => Unit](name) {
	def register(f: S => Unit)(implicit c: PluginContext) = c.register(this, f)
	def actions = get
	def apply(s: S)(implicit c: PluginContext) { for (action <- actions) action(s) }
}

//  A hook that transforms a value
class FilterHook[V, S <: Product](name: String) extends Hook[V => S => V](name) {
	def register(f: V => S => V)(implicit c: PluginContext) = c.register(this, f)
	def filters = get
	def apply(value: V)(s: S)(implicit c: PluginContext): V =
		filters.foldLeft(value)((value: V, filter: V => S => V) => filter(value)(s))
}

/*
//  A hook the converts between two types
class LensHook[O, I](name: String) extends Hook[O => I](name) {
	class LensHookCounterpart extends Hook[I => O](name) {
		def encode = LensHook.this.decode
		def decode = LensHook.this.encode
		def counterpart = LensHook.this
	}
	val counterpart = new LensHookCounterpart

	def registerEncoder(enc: O => I)(implicit c: PluginContext) = c.register(this, enc)
	def registerDecoder(dec: I => O)(implicit c: PluginContext) = c.register(counterpart, dec)

	def encode(o: O): Option[I] = {
		val encoders = get
		...
	}
	
	def decode(i: I): Option[O] = {
		val decoders = counterpart.get
		...
	}
}*/
