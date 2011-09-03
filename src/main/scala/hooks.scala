package hooks

import scala.Product
import scala.collection.mutable.{ListBuffer}

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

//  A hook that fires an action
object ActionHook {
	def apply(name: String) = new SimpleActionHook(name)
	def simple(name: String) = new SimpleActionHook(name)
	def apply[S](name: String) = new ActionHook[S](name)
	def standalone[S](name: String) = new StandaloneActionHook[S](name)
}

class StandaloneActionHook[S](name: String) extends Hook[(S) => Unit](name) {
  val actions = new ListBuffer[(S) => Unit]()

  def registerAction(f: (S) => Unit) = actions += f
  def register(f: (S) => Unit) = actions += f
  
  def apply(value: S): Unit = {
    for (action <- this.actions)
      action(value)
  }
}

class SimpleActionHook(name: String) extends Hook[(PluginContext) => Unit](name) {
	def registerAction(f: (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
	def register(f: (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
	def register(f: => Unit)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor(f).act _)
	
	class Adaptor(f: => Unit) { def act(c: PluginContext) = f }
	
	def actions(implicit c: PluginContext) = get
	def apply()(implicit c: PluginContext) { for (action <- actions) action(c) }
}

class ActionHook[S](name: String) extends Hook[(S) => (PluginContext) => Unit](name) {
	def registerAction(f: (S) => (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
	def register(f: (S, PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).filter _)
	def register(f: (S) => Unit)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor2(f).filter _)
	class Adaptor1(f: (S, PluginContext) => Unit) { def filter(s: S)(c: PluginContext) = f(s, c) }
	class Adaptor2(f: (S) => Unit) { def filter(s: S)(c: PluginContext) = f(s) }
	
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
	
	def register(f: (V, PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, Adaptor1(f).filter _)
	def register(f: (V) => V)(implicit c: PluginContextBuilder) = c.register(this, Adaptor2(f).filter _)
	
	case class Adaptor1(f: (V, PluginContext) => V) { def filter(v: V)(c: PluginContext) = f(v, c) }
	case class Adaptor2(f: (V) => V) { def filter(v: V)(c: PluginContext) = f(v) }
	
	def filters(implicit c: PluginContext) = get
	def apply(value: V)(implicit c: PluginContext): V = {
		filters.foldLeft(value) {
			(value, filter) => filter(value)(c)
		}
	}
}

class FilterHook[V, S](name: String) extends Hook[(V) => (S) => (PluginContext) => V](name) {
	def registerFilter(f: (V) => (S) => (PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, f)
	
	def register(f: (V, S, PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).filter _)
	def register(f: (V, S) => V)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor2(f).filter _)
	def register(f: (V) => V)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor3(f).filter _)
	
	class Adaptor1(f: (V, S, PluginContext) =>V) { def filter(v: V)(s: S)(c: PluginContext) = f(v,s,c) }
	class Adaptor2(f: (V, S) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v,s) }
	class Adaptor3(f: (V) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v) }

	def filters(implicit c: PluginContext) = get
	def apply(value: V)(extra: S)(implicit c: PluginContext): V =
		filters.foldLeft(value) { 
			(value, filter) => filter(value)(extra)(c)
		}
}


//  A hook that selects just one object
abstract class SelectableHook[T](name: String)(selector: (List[T], PluginContext) => Option[T]) extends Hook[T](name) {
	def register(t: T)(implicit c: PluginContextBuilder) = c.register(this, t)

	def selectValue(values: List[T])(implicit c: PluginContext): Option[T]
	def select(implicit c: PluginContext) = selectValue(get)
	def apply()(implicit c: PluginContext) = select
}


//  A hook that collects string fragments and assembles them
object BufferHook {
	def strid(v: String) = v
	def apply(name: String, prefix: String, infix: String, affix: String) = new BufferHook[String](name, prefix, infix, affix, strid)
	def apply(name: String, infix: String) = new BufferHook[String](name, "", infix, "", strid)
	def apply(name: String) = new BufferHook[String](name, "", "", "", strid)
	
	def apply[T](name: String, prefix: String, infix: String, affix: String, f: (T) => String) = new BufferHook[T](name, prefix, infix, affix, f)
	def apply[T](name: String, infix: String, f: (T) => String) = new BufferHook[T](name, "", infix, "", f)
	def apply[T](name: String, f: (T) => String) = new BufferHook[T](name, "", "", "", f)
}

class BufferHook[T](name: String, prefix: String, infix: String, affix: String, fix: (T) => String) extends Hook[(PluginContext) => String](name) {
	val earlyFilters = FilterHook[T](name+" (early filter)")
	val lateFilters = FilterHook[String](name+" (late filter)")

	def registerFragment(f: (PluginContext) => T)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).render _)
	def add(f: (PluginContext) => T)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).render _)
	def add(f: => T)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor2(f).render _)
	def add(nested: BufferHook[_])(implicit c: PluginContextBuilder) = c.register(this, new NestAdaptor(nested).render _)
	
	class Adaptor1(f: (PluginContext) => T) { def render(c: PluginContext): String = fix(earlyFilters(f(c))(c)) }
	class Adaptor2(f: => T) { def render(c: PluginContext): String = fix(earlyFilters(f)(c)) }
	class NestAdaptor(nested: BufferHook[_]) { def render(c: PluginContext): String = nested()(c) }
	
	def fragments(implicit c: PluginContext) = get
	def apply()(implicit c: PluginContext) = {
		val strings = fragments.map((f: (PluginContext) => String) => lateFilters(f(c))(c))
		strings.mkString(prefix, infix, affix)
	}
}


//  A hook that approves or rejects a value
object GuardHook {
  def apply[T](name: String) = new SimpleGuardHook[T](name)
  def apply[T, S](name: String) = new GuardHook[T, S](name)
  def standalone[T](name: String) = new SimpleStandaloneGuardHook[T](name)
  def standalone[T, S](name: String) = new StandaloneGuardHook[T, S](name)
}

class SimpleGuardHook[T](name: String) extends Hook[(T) => (PluginContext) => Boolean](name) {
  def registerGuard(f: (T) => (PluginContext) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, f)
  def register(f: (T, PluginContext) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).guard _)
  def register(f: (T) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor2(f).guard _)
  
  class Adaptor1(f: (T, PluginContext) => Boolean) { def guard(v: T)(c: PluginContext) = f(v,c) }
  class Adaptor2(f: (T) => Boolean) { def guard(v: T)(c: PluginContext) = f(v) }
  
  def guards(implicit c: PluginContext) = get
  def apply(value: T)(implicit c: PluginContext): Boolean = {
    val guards = this.guards
    guards.isEmpty || guards.forall(g => g(value)(c))
  }
}

class GuardHook[T, S](name: String) extends Hook[(T) => (S) => (PluginContext) => Boolean](name) {
  def registerGuard(f: (T) => (S) => (PluginContext) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, f)
  def register(f: (T, S, PluginContext) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).guard _)
  def register(f: (T, S) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor2(f).guard _)
  def register(f: (T) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor3(f).guard _)
  
  class Adaptor1(f: (T, S, PluginContext) => Boolean) { def guard(v: T)(s: S)(c: PluginContext) = f(v,s,c) }
  class Adaptor2(f: (T, S) => Boolean) { def guard(v: T)(s: S)(c: PluginContext) = f(v,s) }
  class Adaptor3(f: (T) => Boolean) { def guard(v: T)(s: S)(c: PluginContext) = f(v) }
  
  def guards(implicit c: PluginContext) = get
  def apply(value: T)(extra: S)(implicit c: PluginContext): Boolean = {
    val guards = this.guards
    guards.isEmpty || guards.forall(g => g(value)(extra)(c))
  }
}

class SimpleStandaloneGuardHook[T](name: String) {
  val guards = new ListBuffer[(T) => Boolean]()
  
  def registerGuard(f: (T) => Boolean) = guards += f
  def register(f: (T) => Boolean) = guards += f
  
  def apply(value: T): Boolean = {
    val guards = this.guards
    guards.isEmpty || guards.forall(g => g(value))
  }
}

class StandaloneGuardHook[T, S](name: String) {
  val guards = new ListBuffer[(T) => (S) => Boolean]()
  
  def registerGuard(f: (T) => (S) => Boolean) = guards += f
  def register(f: (T, S) => Boolean) = guards += new Adaptor1(f).guard _
  def register(f: (T) => Boolean) = guards += new Adaptor2(f).guard _
  
  class Adaptor1(f: (T, S) => Boolean) { def guard(t: T)(s: S) = f(t, s) }
  class Adaptor2(f: (T) => Boolean) { def guard(t: T)(s: S) = f(t) }
  
  def apply(value: T)(extra: S): Boolean = {
    val guards = this.guards
    guards.isEmpty || guards.forall(g => g(value)(extra))
  }
  
  lazy val sync = new SynchronizedStandaloneGuardHook(this)
}

class SynchronizedStandaloneGuardHook[T, S](inner: StandaloneGuardHook[T, S]) {
	def guards = inner.synchronized { inner.guards.toList }
	
	def registerGuard(f: (T) => (S) => Boolean) = inner.synchronized { inner.registerGuard(f) }
	def register(f: (T, S) => Boolean) = inner.synchronized { inner.register(f) }
	def register(f: (T) => Boolean) = inner.synchronized { inner.register(f) }
	
	def apply(value: T)(extra: S) = inner.synchronized { inner.apply(value)(extra) }
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
