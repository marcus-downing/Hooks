package hooks

import scala.Product
import scala.collection.mutable.{ListBuffer}


/**
 * Hooks
 *
 * These objects identify and provide access to the collected functions and objects
 * attached to them.
 */

abstract class Hook[S](val name: String) {
  def _get(implicit c: PluginContext) = c.get(this)
  def logErrors(f: => Unit) = try { f } catch { case x => x.printStackTrace }
}


//  A hook that stores objects of a given type
object ComponentHook {
  def apply[T](name: String) = new ComponentHook[T](name)
  def standalone[T](name: String) = new ComponentHook[T](name)
}

class ComponentHook[T](name: String) extends Hook[T](name) {
  def register(t: T)(implicit c: PluginContextBuilder) = c.register(this, t)
  def apply()(implicit c: PluginContext) = _get
  def components(implicit c: PluginContext) = _get
  def collect[S <: T](implicit c: PluginContext) = _get.collect{ case s: S => s }
}

class StandaloneComponentHook[T](name: String) extends Hook[T](name) {
  val _components = new ListBuffer[T]()
  def register(t: T) = _components += t
  def apply() = _components.toList
  def components = _components.toList
  def collect[S <: T] = _components.collect{ case s: S => s }
}


//  A hook that fires an action
object ActionHook {
  def apply(name: String) = new SimpleActionHook(name)
  def simple(name: String) = new SimpleActionHook(name)
  def apply[S](name: String) = new ActionHook[S](name)
  def standalone[S](name: String) = new StandaloneActionHook[S](name)
}

class SimpleStandaloneActionHook(name: String) extends Hook[() => Unit](name) {
  val actions = new ListBuffer[() => Unit]()
  def registerAction(f: () => Unit) = actions += f
  def delegate(target: SimpleStandaloneActionHook) = actions += target.apply _
  
  def apply() { for (action <- actions) logErrors { action() } }
}

class StandaloneActionHook[S](name: String) extends Hook[(S) => Unit](name) {
  val actions = new ListBuffer[(S) => Unit]()
  def registerAction(f: (S) => Unit) = actions += f
  def register(f: (S) => Unit) = actions += f
  
  def delegate[Q >: S](target: StandaloneActionHook[Q]) = actions += target.apply _
  def delegate(target: SimpleStandaloneActionHook) = actions += { s => target() }
  
  def apply(value: S): Unit = { for (action <- this.actions) logErrors { action(value) } }
}

class SimpleActionHook(name: String) extends Hook[(PluginContext) => Unit](name) {
  def registerAction(f: (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
  def register(f: (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
  def register(f: => Unit)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor(f).act _)
  class Adaptor(f: => Unit) { def act(c: PluginContext) = f }
  
  def delegate(target: SimpleActionHook)(implicit c: PluginContextBuilder) = c.register(this, new Delegate1(target).act _)
  def delegate(target: SimpleStandaloneActionHook)(implicit c: PluginContextBuilder) = c.register(this, new Delegate2(target).act _)
  class Delegate1(target: SimpleActionHook) { def act(c: PluginContext) = target()(c) }
  class Delegate2(target: SimpleStandaloneActionHook) { def act(c: PluginContext) = target() }
  
  def actions(implicit c: PluginContext) = _get
  def apply()(implicit c: PluginContext) { for (action <- actions) logErrors { action(c) } }
}

class ActionHook[S](name: String) extends Hook[(S) => (PluginContext) => Unit](name) {
  def registerAction(f: (S) => (PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, f)
  def register(f: (S, PluginContext) => Unit)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).filter _)
  def register(f: (S) => Unit)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor2(f).filter _)
  class Adaptor1(f: (S, PluginContext) => Unit) { def filter(s: S)(c: PluginContext) = f(s, c) }
  class Adaptor2(f: (S) => Unit) { def filter(s: S)(c: PluginContext) = f(s) }
  
  def delegate[Q >: S](target: ActionHook[Q])(implicit c: PluginContextBuilder) = c.register(this, new Delegate1(target).act _)
  def delegate(target: SimpleActionHook)(implicit c: PluginContextBuilder) = c.register(this, new Delegate2(target).act _)
  def delegate[Q >: S](target: StandaloneActionHook[Q])(implicit c: PluginContextBuilder) = c.register(this, new Delegate3(target).act _)
  def delegate(target: SimpleStandaloneActionHook)(implicit c: PluginContextBuilder) = c.register(this, new Delegate4(target).act _)
  class Delegate1[Q >: S](target: ActionHook[Q]) { def act(s: S)(c: PluginContext) = target(s)(c) }
  class Delegate2(target: SimpleActionHook) { def act(s: S)(c: PluginContext) = target()(c) }
  class Delegate3[Q >: S](target: StandaloneActionHook[Q]) { def act(s: S)(c: PluginContext) = target(s) }
  class Delegate4(target: SimpleStandaloneActionHook) { def act(s: S)(c: PluginContext) = target() }
  
  def actions(implicit c: PluginContext) = _get
  def apply(s: S)(implicit c: PluginContext) { for (action <- actions) logErrors { action(s)(c) } }
}

//  A hook that transforms a value
object FilterHook {
  def apply[V](name: String) = new SimpleFilterHook[V](name)
  def apply[V, S](name: String) = new FilterHook[V, S](name)
  def standalone[V](name: String) = new StandaloneSimpleFilterHook[V](name)
  def standalone[V, S](name: String) = new StandaloneFilterHook[V, S](name)
}

class SimpleFilterHook[V](name: String) extends Hook[(V) => (PluginContext) => V](name) {
  def registerFilter(f: (V) => (PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, f)
  
  def register(f: (V, PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, Adaptor1(f).filter _)
  def register(f: (V) => V)(implicit c: PluginContextBuilder) = c.register(this, Adaptor2(f).filter _)
  
  case class Adaptor1(f: (V, PluginContext) => V) { def filter(v: V)(c: PluginContext) = f(v, c) }
  case class Adaptor2(f: (V) => V) { def filter(v: V)(c: PluginContext) = f(v) }
  
  def filters(implicit c: PluginContext) = _get
  def apply(value: V)(implicit c: PluginContext): V =
    filters.foldLeft(value) { (value, filter) => filter(value)(c) }
}

class FilterHook[V, S](name: String) extends Hook[(V) => (S) => (PluginContext) => V](name) {
  def registerFilter(f: (V) => (S) => (PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, f)
  
  def register(f: (V, S, PluginContext) => V)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).filter _)
  def register(f: (V, S) => V)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor2(f).filter _)
  def register(f: (V) => V)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor3(f).filter _)
  
  class Adaptor1(f: (V, S, PluginContext) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v,s,c) }
  class Adaptor2(f: (V, S) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v,s) }
  class Adaptor3(f: (V) => V) { def filter(v: V)(s: S)(c: PluginContext) = f(v) }

  def filters(implicit c: PluginContext) = _get
  def apply(value: V)(extra: S)(implicit c: PluginContext): V =
    filters.foldLeft(value) { (value, filter) => filter(value)(extra)(c) }
}

class StandaloneSimpleFilterHook[V](name: String) extends Hook[(V) => V](name) {
  val _filters = new ListBuffer[(V) => V]()
  def registerFilter(f: (V) => V) = _filters += f
  def register(f: (V) => V) = _filters += f
  
  def filters = _filters.toList
  def apply(value: V): V = filters.foldLeft(value) { (value, filter) => filter(value) }
}

class StandaloneFilterHook[V, S](name: String) extends Hook[(V) => (S) => V](name) {
  val _filters = new ListBuffer[(V) => (S) => V]()
  def registerFilter(f: (V) => (S) => V) = _filters += f
  def register(f: (V, S) => V) = _filters += new Adaptor1(f).filter _
  def register(f: (V) => V) = _filters += new Adaptor2(f).filter _
  
  class Adaptor1(f: (V, S) => V) { def filter(v: V)(s: S) = f(v,s) }
  class Adaptor2(f: (V) => V) { def filter(v: V)(s: S) = f(v) }
  
  def filters = _filters.toList
  def apply(value: V)(extra: S): V = filters.foldLeft(value) { (value, filter) => filter(value)(extra) }
}


//  A hook that selects just one of the registered objects
object SelectableHook {
  def apply[T](name: String)(selector: (List[T], PluginContext) => Option[T]) = new SimpleSelectableHook(name)(selector)
  def apply[T, S](name: String)(selector: (List[T], S, PluginContext) => Option[T]) = new SelectableHook(name)(selector)
  def standalone[T](name: String)(selector: (List[T]) => Option[T]) = new SimpleStandaloneSelectableHook(name)(selector)
  def standalone[T, S](name: String)(selector: (List[T], S) => Option[T]) = new StandaloneSelectableHook(name)(selector)
}

class SimpleSelectableHook[T](name: String)(selector: (List[T], PluginContext) => Option[T]) extends Hook[T](name) {
  val guard = GuardHook.standalone[T](name+" (guard)")
  def register(t: T)(implicit c: PluginContextBuilder) = c.register(this, t)
  
  def items(implicit c: PluginContext): List[T] = guard(_get).toList
  def apply()(implicit c: PluginContext) = selector(items, c)
}

class SelectableHook[T, S](name: String)(selector: (List[T], S, PluginContext) => Option[T]) extends Hook[T](name) {
  val guard = GuardHook.standalone[T, S](name+" (guard)")
  def register(t: T)(implicit c: PluginContextBuilder) = c.register(this, t)
  
  def items(extra: S)(implicit c: PluginContext): List[T] = guard(_get)(extra).toList
  def apply(extra: S)(implicit c: PluginContext) = selector(items(extra), extra, c)
}

class SimpleStandaloneSelectableHook[T](name: String)(selector: (List[T]) => Option[T]) extends Hook[T](name) {
  private val _items = new ListBuffer[T]()
  val guard = GuardHook.standalone[T](name+" (guard)")
  def register(t: T) = _items += t

  def items: List[T] = guard(_items).toList
  def apply() = selector(items)
}

class StandaloneSelectableHook[T, S](name: String)(selector: (List[T], S) => Option[T]) extends Hook[T](name) {
  private val _items = new ListBuffer[T]()
  val guard = GuardHook.standalone[T, S](name+" (guard)")
  def register(t: T) = _items += t

  def items(extra: S): List[T] = guard(_items.toList)(extra).toList
  def apply(extra: S) = selector(items(extra), extra)
}


//  A hook that collects fragments and assembles them into one string
object BufferHook {
  def strid(v: String) = v
  
  def apply(name: String, prefix: String, infix: String, affix: String) = new BufferHook[String](name, prefix, infix, affix, strid)
  def apply(name: String, infix: String) = new BufferHook[String](name, "", infix, "", strid)
  def apply(name: String) = new BufferHook[String](name, "", "", "", strid)
  
  def apply[T](name: String, prefix: String, infix: String, affix: String, f: (T) => String) = new BufferHook[T](name, prefix, infix, affix, f)
  def apply[T](name: String, infix: String, f: (T) => String) = new BufferHook[T](name, "", infix, "", f)
  def apply[T](name: String, f: (T) => String) = new BufferHook[T](name, "", "", "", f)
  
  def standalone(name: String, prefix: String, infix: String, affix: String) = new StandaloneBufferHook[String](name, prefix, infix, affix, strid)
  def standalone(name: String, infix: String) = new StandaloneBufferHook[String](name, "", infix, "", strid)
  def standalone(name: String) = new StandaloneBufferHook[String](name, "", "", "", strid)
  
  def standalone[T](name: String, prefix: String, infix: String, affix: String, f: (T) => String) = new StandaloneBufferHook[T](name, prefix, infix, affix, f)
  def standalone[T](name: String, infix: String, f: (T) => String) = new StandaloneBufferHook[T](name, "", infix, "", f)
  def standalone[T](name: String, f: (T) => String) = new StandaloneBufferHook[T](name, "", "", "", f)
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
  
  def fragments(implicit c: PluginContext) = _get
  def apply()(implicit c: PluginContext) = {
    val strings = fragments.map((f: (PluginContext) => String) => lateFilters(f(c))(c))
    strings.mkString(prefix, infix, affix)
  }
}

class StandaloneBufferHook[T](name: String, prefix: String, infix: String, affix: String, fix: (T) => String) extends Hook[() => String](name) {
  val _producers = new ListBuffer[() => String]()
  val earlyFilters = FilterHook.standalone[T](name+" (early filter)")
  val lateFilters = FilterHook.standalone[String](name+" (late filter)")

  def registerFragment(f: => T) = _producers += (new Adaptor(f).render _)
  def add(f: => T) = _producers += (new Adaptor(f).render _)
  def add(nested: StandaloneBufferHook[_]) = _producers += (new NestAdaptor(nested).render _)
  
  class Adaptor(f: => T) { def render(): String = fix(earlyFilters(f)) }
  class NestAdaptor(nested: StandaloneBufferHook[_]) { def render(): String = nested() }
  
  def fragments = _producers.toList
  def apply() = {
    val strings = fragments.map((f: () => String) => lateFilters(f()))
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
  
  def guards(implicit c: PluginContext) = _get
  def apply(value: T)(implicit c: PluginContext): Boolean = {
    val guards = this.guards
    guards.isEmpty || guards.forall(g => g(value)(c))
  }
  def apply(values: Seq[T])(implicit c: PluginContext): Seq[T] = values.filter(v => this(v))
  def apply(values: Option[T])(implicit c: PluginContext): Option[T] = values.filter(v => this(v))
  def apply(values: List[T])(implicit c: PluginContext): List[T] = values.filter(v => this(v))
}

class GuardHook[T, S](name: String) extends Hook[(T) => (S) => (PluginContext) => Boolean](name) {
  def registerGuard(f: (T) => (S) => (PluginContext) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, f)
  def register(f: (T, S, PluginContext) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor1(f).guard _)
  def register(f: (T, S) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor2(f).guard _)
  def register(f: (T) => Boolean)(implicit c: PluginContextBuilder) = c.register(this, new Adaptor3(f).guard _)
  
  class Adaptor1(f: (T, S, PluginContext) => Boolean) { def guard(v: T)(s: S)(c: PluginContext) = f(v,s,c) }
  class Adaptor2(f: (T, S) => Boolean) { def guard(v: T)(s: S)(c: PluginContext) = f(v,s) }
  class Adaptor3(f: (T) => Boolean) { def guard(v: T)(s: S)(c: PluginContext) = f(v) }
  
  def guards(implicit c: PluginContext) = _get
  def apply(value: T)(extra: S)(implicit c: PluginContext): Boolean = {
    val guards = this.guards
    guards.isEmpty || guards.forall(g => g(value)(extra)(c))
  }
  def apply(values: Seq[T])(extra: S)(implicit c: PluginContext): Seq[T] = values.filter(v => this(v)(extra))
  def apply(values: Option[T])(extra: S)(implicit c: PluginContext): Option[T] = values.filter(v => this(v)(extra))
  def apply(values: List[T])(extra: S)(implicit c: PluginContext): List[T] = values.filter(v => this(v)(extra))
}

class SimpleStandaloneGuardHook[T](name: String) {
  val _guards = new ListBuffer[(T) => Boolean]()
  
  def registerGuard(f: (T) => Boolean) = _guards += f
  def register(f: (T) => Boolean) = _guards += f
  
  def apply(value: T): Boolean = {
    val guards = _guards.toList
    guards.isEmpty || guards.forall(g => g(value))
  }
  def apply(values: Seq[T]): Seq[T] = values.filter(v => this(v))
  def apply(values: Option[T]): Option[T] = values.filter(v => this(v))
  def apply(values: List[T]): List[T] = values.filter(v => this(v))
}

class StandaloneGuardHook[T, S](name: String) {
  val _guards = new ListBuffer[(T) => (S) => Boolean]()
  
  def registerGuard(f: (T) => (S) => Boolean) = _guards += f
  def register(f: (T, S) => Boolean) = _guards += new Adaptor1(f).guard _
  def register(f: (T) => Boolean) = _guards += new Adaptor2(f).guard _
  
  class Adaptor1(f: (T, S) => Boolean) { def guard(t: T)(s: S) = f(t, s) }
  class Adaptor2(f: (T) => Boolean) { def guard(t: T)(s: S) = f(t) }
  
  def apply(value: T)(extra: S): Boolean = {
    val guards = _guards.toList
    guards.isEmpty || guards.forall(g => g(value)(extra))
  }
  def apply(values: Seq[T])(extra: S): Seq[T] = values.filter(v => this(v)(extra))
  def apply(values: Option[T])(extra: S): Option[T] = values.filter(v => this(v)(extra))
  def apply(values: List[T])(extra: S): List[T] = values.filter(v => this(v)(extra))
  
  lazy val sync = new SynchronizedStandaloneGuardHook(this)
}

class SynchronizedStandaloneGuardHook[T, S](inner: StandaloneGuardHook[T, S]) {
  def guards = inner.synchronized { inner._guards.toList }
  
  def registerGuard(f: (T) => (S) => Boolean) = inner.synchronized { inner.registerGuard(f) }
  def register(f: (T, S) => Boolean) = inner.synchronized { inner.register(f) }
  def register(f: (T) => Boolean) = inner.synchronized { inner.register(f) }
  
  def apply(value: T)(extra: S) = inner.synchronized { inner.apply(value)(extra) }
  def apply(values: Seq[T])(extra: S): Seq[T] = values.filter(v => this(v)(extra))
  def apply(values: Option[T])(extra: S): Option[T] = values.filter(v => this(v)(extra))
  def apply(values: List[T])(extra: S): List[T] = values.filter(v => this(v)(extra))
}


//  A hook that converts reversibly between two types, Inner and Outer
//  decode() produces an Outer value from an Inner
//  encode() combines the Outer value with an original Inner to produce a revised Inner
/*
class SimpleLensHook[I, O](name: String) extends Hook[Dec](name) {
  type Dec = (I) => (PluginContext) => O
  type Enc = (O, I) => (PluginContext) => I
  
  class Counterpart extends Hook[Enc](name) {
    def decode = LensHook.this.encode
    def encode = LensHook.this.decode
    def counterpart = LensHook.this
  }
  
  val counterpart = new LensHookCounterpart

  def defaultValue: I
  def registerDecoder(dec: Dec)(implicit c: PluginContextBuilder) = c.register(this, dec)
  def registerEncoder(enc: Enc)(implicit c: PluginContextBuilder) = c.register(counterpart, enc)

  def decoders = counterpart.get
  def encoders = get
  
  def decode(inner: I)(implicit c: PluginContext): Option[O] = {
    val values = for (dec <- decoders) yield dec(inner)
    values.flatten.headOption
  }
  
  def encode(outer: O, original: I)(implicit c: PluginContext): Option[I] = {
    val values = for (enc <- encoders) yield enc(outer, original)
    values.flatten.headOption
  }
}

class LensHook[I, O, S](name: String) extends Hook[Dec](name) {
  type Dec = (I, S) => (PluginContext) => O
  type Enc = (O, I, S) => (PluginContext) => I
  
  class Counterpart extends Hook[Enc](name) {
    def decode = LensHook.this.encode
    def encode = LensHook.this.decode
    def counterpart = LensHook.this
  }
  
  val counterpart = new LensHookCounterpart

  def defaultValue: I
  def registerDecoder(dec: Dec)(implicit c: PluginContextBuilder) = c.register(this, dec)
  def registerEncoder(enc: Enc)(implicit c: PluginContextBuilder) = c.register(counterpart, enc)

  def decoders = counterpart.get
  def encoders = get
  
  def decode(inner: I, extra: S)(implicit c: PluginContext): Option[O] = {
    val values = for (dec <- decoders) yield dec(inner, extra)
    values.flatten.headOption
  }
  
  def encode(outer: O, original: I, extra: S)(implicit c: PluginContext): Option[I] = {
    val values = for (enc <- encoders) yield enc(outer, original, extra)
    values.flatten.headOption
  }
}
*/


object ResourceTrackerHook {
  def apply[T, ID](name: String) = new ResourceTrackerHook[T, ID](name)
  def standalone[T, ID](name: String) = new StandaloneResourceTrackerHook[T, ID](name)
}

class ResourceProvider[T, ID](val id: Option[ID], open_fn: => T, val close: (T) => Unit) {
  def open() = open_fn
}

class ResourceTrackerHook[T, ID](name: String) extends Hook[ResourceProvider[T, ID]](name) {
  type P = ResourceProvider[T, ID]
  def providers(implicit c: PluginContext) = _get
  
  def registerProvider(provider: P)(implicit c: PluginContextBuilder) = c.register(this, provider)
  def register(open: => T, close: (T) => Unit = { t => }, id: Option[ID] = None)(implicit c: PluginContextBuilder) = c.register(this, new P(id, open, close))
  
  def apply()(implicit c: PluginContext) = _get.headOption
  def apply(id: ID)(implicit c: PluginContext) = _get.filter(_.id == Some(id)).headOption
  
  def _list(implicit c: PluginContext) = new ResourceProviderList[T, ID] {
    def all = providers
    def get = apply()
    def get(id: ID) = apply(id)
  }
  def tracker(implicit c: PluginContext) = new ResourceTracker(_list)
}

class StandaloneResourceTrackerHook[T, ID](name: String) extends Hook[ResourceProvider[T, ID]](name) {
  type P = ResourceProvider[T, ID]
  
  val _providers = new ListBuffer[P]()
  def providers = _providers.toList
  
  def registerProvider(provider: P)(implicit c: PluginContextBuilder) = _providers += provider
  def register(open: => T, close: (T) => Unit = { t => }, id: Option[ID] = None)(implicit c: PluginContextBuilder) = _providers += new P(id, open, close)
  
  def apply() = providers.headOption
  def apply(id: ID) = providers.filter(_.id == Some(id)).headOption
  
  def _list = new ResourceProviderList[T, ID] {
    def all = providers
    def get = apply()
    def get(id: ID) = apply(id)
  }
  def tracker = new ResourceTracker(_list)
}

object ResourceProviderList {
  def empty[T, ID] = new ResourceProviderList[T, ID] {
    def all = Nil
    def get = None
    def get(id: ID) = None
  }
}

trait ResourceProviderList[T, ID] {
  def all: List[ResourceProvider[T, ID]]
  def get: Option[ResourceProvider[T, ID]]
  def get(id: ID): Option[ResourceProvider[T, ID]]
}

class ResourceTracker[T, ID](list: ResourceProviderList[T, ID]) {
  def this() = this(ResourceProviderList.empty[T, ID])

  type P = ResourceProvider[T, ID]
  def providers = list.all

  val _stack = new ListBuffer[(Option[ID], T)]()
  def stack = _stack.toList
  def _using[R](provider: P, f: => R): R = {
    val value: T = provider.open
    _stack.prepend((provider.id, value))
    try { f } finally { _stack.remove(0); provider.close(value) }
  }
  
  def using[R](f: => R) = if (!has()) _using(list.get.get, f) else f
  def using[R](id: ID)(f: => R) = if (!has(id)) _using(list.get(id).get, f) else f
  def using[R](open: => T, close: (T) => Unit = { t => }, optional: Boolean = false)(f: => R) =
    if (optional && !has()) _using(new P(None, open, close), f) else f
  
  def has() = !_stack.isEmpty
  def has(id: ID) = !this(id).isEmpty
  
  def apply(): T = _stack.headOption.map(_._2).get
  def apply(id: ID): Option[T] = _stack.filter(_._1 == Some(id)).map(_._2).headOption
}






