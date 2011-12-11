package hooks

//import Hooks._
import Imports._
import scala.Product
import scala.collection.mutable.{ListBuffer}


/**
 * Hooks
 *
 * These objects identify and provide access to the collected functions and objects
 * attached to them.
 */

abstract class Hook[S](val name: String) {
  def _get = HookContext { cx => cx.get(this) }
  def logErrors(f: => Unit) = try { f } catch { case x => x.printStackTrace }
}



//  A hook that stores objects of a given type
object ComponentHook {
  def apply[T](name: String) = new ComponentHook[T](name)
  
  object standalone {
    def apply[T](name: String) = new ComponentHook[T](name)
  }
}

class ComponentHook[T](name: String) extends Hook[T](name) {
  def register(t: T): Unit = ContextBuilder { cb => cb.register(this, t) }
  def apply() = _get
  def components = _get
  def collect[S <: T] = _get.collect{ case s: S => s }
}

class StandaloneComponentHook[T](name: String) extends Hook[T](name) {
  val _components = new ListBuffer[T]()
  def register(t: T): Unit = _components += t
  def apply() = _components.toList
  def components = _components.toList
  def collect[S <: T] = _components.collect{ case s: S => s }
}


//  A hook that fires an action
object ActionHook {  
  def simple(name: String) = new ActionHook0(name)
  def apply[A](name: String)(implicit d: D1) = new ActionHook[A](name)
  
  object standalone {
    def simple(name: String) = new StandaloneActionHook0(name)
    def apply[A](name: String)(implicit d: D1) = new StandaloneActionHook[A](name)
  }
}


class StandaloneActionHook[S](name: String) extends Hook[(S) => Unit](name) {
  val actions = new ListBuffer[(S) => Unit]()
  def register(f: (S) => Unit): Unit = actions += f
  def register(fn: S => Unit)(implicit d: D1): Unit = actions += new Adapter1(fn).apply _
  def register(fn: () => Unit)(implicit d: D3): Unit = actions += new Adapter2(fn).apply _
  def register(fn: => Unit)(implicit d: D2): Unit = actions += new Adapter2(fn).apply _
  
  class Adapter1(fn: S => Unit) { def apply(s: S)(cx: HookContext): Unit = fn(s) }
  class Adapter2(fn: => Unit) { def apply(s: S)(cx: HookContext): Unit = fn }
  
  //def delegate[Q >: S](target: StandaloneActionHook[Q]) = actions += target.apply _
  
  def apply(value: S): Unit = { for (action <- this.actions) logErrors { action(value) } }
}

class StandaloneActionHook0(name: String) extends StandaloneActionHook[Nil.type](name) {
  def apply() { apply(Nil); }
}

class ActionHook[S](name: String) extends Hook[S => Unit](name) {
  def register(fn: S => Unit): Unit = ContextBuilder { cb => cb.register(this, fn) }
  def register(fn: => Unit)(implicit d: D2): Unit = ContextBuilder { cb => cb.register(this, new Adapter2(fn).apply _) }
  
  class Adapter2(fn: => Unit) { def apply(s: S) { fn } }
  
  //def delegate[Q >: S](target: ActionHook[Q]) = actions += target.apply _
  
  def actions = _get
  def apply(s: S) { for (action <- actions) logErrors { action(s) } }
}

class ActionHook0(name: String) extends ActionHook[Nil.type](name) {
  def apply() { apply(Nil) }
}


//  A hook that transforms a value
object FilterHook {
  def apply[V](name: String) = new FilterHook0[V](name)
  def apply[V, S](name: String)(implicit d: D1) = new FilterHook[V, S](name)
  
  object standalone {
    def apply[V](name: String) = new StandaloneFilterHook0[V](name)
    def apply[V, S](name: String) = new StandaloneFilterHook[V, S](name)
  }
}

class FilterHook[V, S](name: String) extends Hook[(V, S) => V](name) {
  def register(f: (V, S) => V): Unit = ContextBuilder { cb => cb.register(this, f) }
  def register(f: (V) => V): Unit = ContextBuilder { cb => cb.register(this, new Adaptor3(f).filter _) }
  
  //class Adaptor2(f: (V, S) => V) { def filter(v: V, s: S) = f(v,s) }
  class Adaptor3(f: (V) => V) { def filter(v: V, s: S) = f(v) }
  //class Adaptor4(f: (V, HookContext) => V) { def filter(v: V, s: S, cx: HookContext) = f(v, cx) }

  def filters = _get
  def apply(value: V, extra: S): V = filters.foldLeft(value) { (value, filter) => filter(value, extra) }
}

class FilterHook0[V](name: String) extends FilterHook[V, Nil.type](name: String) {
  def apply(value: V): V = apply(value, Nil)
}

class StandaloneFilterHook[V, S](name: String) extends Hook[(V, S) => V](name) {
  val _filters = new ListBuffer[(V, S) => V]()
  def register(f: (V, S) => V): Unit = _filters += f
  def register(f: (V) => V): Unit = _filters += new Adaptor2(f).filter _
  
  class Adaptor2(f: (V) => V) { def filter(v: V, s: S) = f(v) }
  
  def filters = _filters.toList
  def apply(value: V, extra: S): V = filters.foldLeft(value) { (value, filter) => filter(value, extra) }
}

class StandaloneFilterHook0[V](name: String) extends StandaloneFilterHook[V, Nil.type](name: String) {
  def apply(value: V): V = apply(value, Nil)
}


//  A hook that selects just one of the registered objects
object SelectableHook {
  def apply[T](name: String)(selector: (List[T]) => Option[T]) = new SelectableHook0(name, selector)
  def apply[T, S](name: String)(selector: (List[T], S) => Option[T]) = new SelectableHook(name, selector)
  
  object standalone {
    def apply[T](name: String)(selector: (List[T]) => Option[T]) = new StandaloneSelectableHook0(name, selector)
    def apply[T, S](name: String)(selector: (List[T], S) => Option[T]) = new StandaloneSelectableHook(name, selector)
  }
}

/*
class SimpleSelectableHook[T](name: String)(selector: (List[T], HookContext) => Option[T]) extends Hook[T](name) {
  val guard = GuardHook.standalone[T](name+" (guard)")
  def register(t: T)(implicit cx: ContextBuilder) = cx.register(this, t)
  
  def items(implicit cx: HookContext): List[T] = guard(_get).toList
  def apply()(implicit cx: HookContext) = selector(items, cx)
}
*/

class SelectableHook[T, S](name: String, selector: (List[T], S) => Option[T]) extends Hook[T](name) {
  val guard = GuardHook.standalone[T, S](name+" (guard)")
  def register(t: T): Unit = ContextBuilder { cb => cb.register(this, t) }
  
  def items(extra: S): List[T] = guard(_get, extra).toList
  def apply(extra: S): Option[T] = selector(items(extra), extra)
}

class SelectableHook0[T](name: String, selector: (List[T]) => Option[T]) extends SelectableHook[T, Nil.type](name, new SelectableHook0Adaptor(selector).apply _) {
  def apply(): Option[T] = apply(Nil)
}

class SelectableHook0Adaptor[T](selector: List[T] => Option[T]) {
  def apply(items: List[T], nil: Nil.type): Option[T] = selector(items)
}

/*
class SimpleStandaloneSelectableHook[T](name: String)(selector: (List[T]) => Option[T]) extends Hook[T](name) {
  private val _items = new ListBuffer[T]()
  val guard = GuardHook.standalone[T](name+" (guard)")
  def register(t: T) = _items += t

  def items: List[T] = guard(_items).toList
  def apply() = selector(items)
}
*/

class StandaloneSelectableHook[T, S](name: String, selector: (List[T], S) => Option[T]) extends Hook[T](name) {
  private val _items = new ListBuffer[T]()
  val guard = GuardHook.standalone[T, S](name+" (guard)")
  def register(t: T): Unit = _items += t

  def items(extra: S): List[T] = guard(_items.toList, extra).toList
  def apply(extra: S) = selector(items(extra), extra)
}

class StandaloneSelectableHook0[T](name: String, selector: (List[T]) => Option[T]) extends StandaloneSelectableHook[T, Nil.type](name, new StandaloneSelectableHook0Adaptor(selector).apply _) {
  def apply(): Option[T] = apply(Nil)
}

class StandaloneSelectableHook0Adaptor[T](selector: (List[T]) => Option[T]) {
  def apply(items: List[T], nil: Nil.type): Option[T] = selector(items)
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
  
  object standalone {
    def apply(name: String, prefix: String, infix: String, affix: String) = new StandaloneBufferHook[String](name, prefix, infix, affix, strid)
    def apply(name: String, infix: String) = new StandaloneBufferHook[String](name, "", infix, "", strid)
    def apply(name: String) = new StandaloneBufferHook[String](name, "", "", "", strid)
  
    def apply[T](name: String, prefix: String, infix: String, affix: String, f: (T) => String) = new StandaloneBufferHook[T](name, prefix, infix, affix, f)
    def apply[T](name: String, infix: String, f: (T) => String) = new StandaloneBufferHook[T](name, "", infix, "", f)
    def apply[T](name: String, f: (T) => String) = new StandaloneBufferHook[T](name, "", "", "", f)
  }
}

class BufferHook[T](name: String, prefix: String, infix: String, affix: String, fix: (T) => String) extends Hook[Function0[String]](name) {
  val earlyFilters = FilterHook[T](name+" (early filter)")
  val lateFilters = FilterHook[String](name+" (late filter)")

  //def add(f: (HookContext) => T): Unit = ContextBuilder { cb => cb.register(this, new Adaptor1(f).render _) }
  def add(f: => T): Unit = ContextBuilder { cb => cb.register(this, new Adaptor2(f).render _) }
  def add(nested: BufferHook[_]): Unit = ContextBuilder { cb => cb.register(this, new NestAdaptor(nested).render _) }
  
  //class Adaptor1(f: (HookContext) => T) { def render(cx: HookContext): String = fix(earlyFilters(f(cx))(cx)) }
  class Adaptor2(f: => T) { def render(): String = fix(earlyFilters(f)) }
  class NestAdaptor(nested: BufferHook[_]) { def render(): String = nested() }
  
  def fragments = _get
  def apply() = {
    val strings = fragments.map { (f: Function0[String]) => lateFilters(f()) }
    strings.mkString(prefix, infix, affix)
  }
}

class StandaloneBufferHook[T](name: String, prefix: String, infix: String, affix: String, fix: (T) => String) extends Hook[Function0[String]](name) {
  val _producers = new ListBuffer[() => String]()
  val earlyFilters = FilterHook.standalone[T](name+" (early filter)")
  val lateFilters = FilterHook.standalone[String](name+" (late filter)")

  def add(f: => T): Unit = _producers += (new Adaptor(f).render _)
  def add(nested: StandaloneBufferHook[_]): Unit = _producers += (new NestAdaptor(nested).render _)
  
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
  def apply[T](name: String) = new GuardHook0[T](name)
  def apply[T, S](name: String) = new GuardHook[T, S](name)
  
  object standalone {
    def apply[T](name: String) = new StandaloneGuardHook0[T](name)
    def apply[T, S](name: String) = new StandaloneGuardHook[T, S](name)
  }
}

class GuardHook[T, S](name: String) extends Hook[(T, S) => Boolean](name) {
  def register(f: (T, S) => Boolean)(implicit d: D1): Unit = ContextBuilder { cb => cb.register(this, new Adaptor2(f).guard _) }
  def register(f: (T) => Boolean)(implicit d: D2): Unit = ContextBuilder { cb => cb.register(this, new Adaptor3(f).guard _) }
  
  class Adaptor2(f: (T, S) => Boolean) { def guard(v: T, s: S) = f(v,s) }
  class Adaptor3(f: (T) => Boolean) { def guard(v: T, s: S) = f(v) }
  
  def guards = _get
  def apply(value: T, extra: S): Boolean = {
    val guards = this.guards
    guards.isEmpty || guards.forall(g => g(value, extra))
  }
  def apply(values: Seq[T], extra: S): Seq[T] = values.filter(v => this(v, extra))
  def apply(values: Option[T], extra: S): Option[T] = values.filter(v => this(v, extra))
  def apply(values: List[T], extra: S): List[T] = values.filter(v => this(v, extra))
}

class GuardHook0[T](name: String) extends GuardHook[T, Nil.type](name) {
  def apply(value: T): Boolean = apply(value, Nil)
  def apply(values: Seq[T]): Seq[T] = values.filter(v => this(v))
  def apply(values: Option[T]): Option[T] = values.filter(v => this(v))
  def apply(values: List[T]): List[T] = values.filter(v => this(v))
}

class StandaloneGuardHook[T, S](name: String) extends Hook[(T, S) => Boolean](name) {
  val _guards = new ListBuffer[(T, S) => Boolean]()
  
  def register(f: (T, S) => Boolean): Unit = _guards += f
  def register(f: (T) => Boolean)(implicit d: D1): Unit = _guards += new Adaptor2(f).guard _
  
  class Adaptor2(f: (T) => Boolean) { def guard(t: T, s: S) = f(t) }
  
  def apply(value: T, extra: S): Boolean = {
    val guards = _guards.toList
    guards.isEmpty || guards.forall(g => g(value, extra))
  }
  def apply(values: Seq[T], extra: S): Seq[T] = values.filter(v => this(v, extra))
  def apply(values: Option[T], extra: S): Option[T] = values.filter(v => this(v, extra))
  def apply(values: List[T], extra: S): List[T] = values.filter(v => this(v, extra))
  
  lazy val sync = new SynchronizedStandaloneGuardHook(this)
}

class StandaloneGuardHook0[T](name: String) extends StandaloneGuardHook[T, Nil.type](name) {
  def apply(value: T): Boolean = apply(value, Nil)
  def apply(values: Seq[T]): Seq[T] = values.filter(v => this(v))
  def apply(values: Option[T]): Option[T] = values.filter(v => this(v))
  def apply(values: List[T]): List[T] = values.filter(v => this(v))
}

class SynchronizedStandaloneGuardHook[T, S](inner: StandaloneGuardHook[T, S]) {
  def guards = inner.synchronized { inner._guards.toList }
  
  def register(f: (T, S) => Boolean): Unit = inner.synchronized { inner.register(f) }
  def register(f: (T) => Boolean): Unit = inner.synchronized { inner.register(f) }
  
  def apply(value: T, extra: S) = inner.synchronized { inner.apply(value, extra) }
  def apply(values: Seq[T], extra: S): Seq[T] = values.filter(v => this(v, extra))
  def apply(values: Option[T], extra: S): Option[T] = values.filter(v => this(v, extra))
  def apply(values: List[T], extra: S): List[T] = values.filter(v => this(v, extra))
}


//  A hook that converts reversibly between two types, Inner and Outer
//  decode() produces an Outer value from an Inner
//  encode() combines the Outer value with an original Inner to produce a revised Inner
/*
class SimpleLensHook[I, O](name: String) extends Hook[Dec](name) {
  type Dec = (I) => (HookContext) => O
  type Enc = (O, I) => (HookContext) => I
  
  class Counterpart extends Hook[Enc](name) {
    def decode = LensHook.this.encode
    def encode = LensHook.this.decode
    def counterpart = LensHook.this
  }
  
  val counterpart = new LensHookCounterpart

  def defaultValue: I
  def registerDecoder(dec: Dec)(implicit cx: ContextBuilder) = cx.register(this, dec)
  def registerEncoder(enc: Enc)(implicit cx: ContextBuilder) = cx.register(counterpart, enc)

  def decoders = counterpart.get
  def encoders = get
  
  def decode(inner: I)(implicit cx: HookContext): Option[O] = {
    val values = for (dec <- decoders) yield dec(inner)
    values.flatten.headOption
  }
  
  def encode(outer: O, original: I)(implicit cx: HookContext): Option[I] = {
    val values = for (enc <- encoders) yield enc(outer, original)
    values.flatten.headOption
  }
}

class LensHook[I, O, S](name: String) extends Hook[Dec](name) {
  type Dec = (I, S) => (HookContext) => O
  type Enc = (O, I, S) => (HookContext) => I
  
  class Counterpart extends Hook[Enc](name) {
    def decode = LensHook.this.encode
    def encode = LensHook.this.decode
    def counterpart = LensHook.this
  }
  
  val counterpart = new LensHookCounterpart

  def defaultValue: I
  def registerDecoder(dec: Dec)(implicit cx: ContextBuilder) = cx.register(this, dec)
  def registerEncoder(enc: Enc)(implicit cx: ContextBuilder) = cx.register(counterpart, enc)

  def decoders = counterpart.get
  def encoders = get
  
  def decode(inner: I, extra: S)(implicit cx: HookContext): Option[O] = {
    val values = for (dec <- decoders) yield dec(inner, extra)
    values.flatten.headOption
  }
  
  def encode(outer: O, original: I, extra: S)(implicit cx: HookContext): Option[I] = {
    val values = for (enc <- encoders) yield enc(outer, original, extra)
    values.flatten.headOption
  }
}
*/


object ResourceTrackerHook {
  def apply[T, ID](name: String) = new ResourceTrackerHook[T, ID](name)
  object standalone {
    def apply[T, ID](name: String) = new StandaloneResourceTrackerHook[T, ID](name)
  }
}

class ResourceProvider[T, ID](val id: Option[ID], open_fn: => T, val close: (T) => Unit) {
  def open() = open_fn
}

class ResourceTrackerHook[T, ID](name: String) extends Hook[ResourceProvider[T, ID]](name) {
  type P = ResourceProvider[T, ID]
  def providers(implicit cx: HookContext) = _get
  
  def register(provider: P): Unit = ContextBuilder { cb => cb.register(this, provider) }
  def register(open: => T, close: (T) => Unit = { t => }, id: Option[ID] = None): Unit = ContextBuilder { cb => cb.register(this, new P(id, open, close)) }
  
  def apply()(implicit cx: HookContext) = _get.headOption
  def apply(id: ID)(implicit cx: HookContext) = _get.filter(_.id == Some(id)).headOption
  
  def _list(implicit cx: HookContext) = new ResourceProviderList[T, ID] {
    def all = providers
    def get = apply()
    def get(id: ID) = apply(id)
  }
  def tracker(implicit cx: HookContext) = new ResourceTracker(_list)
}

class StandaloneResourceTrackerHook[T, ID](name: String) extends Hook[ResourceProvider[T, ID]](name) {
  type P = ResourceProvider[T, ID]
  
  val _providers = new ListBuffer[P]()
  def providers = _providers.toList
  
  def register(provider: P): Unit = _providers += provider
  def register(open: => T, close: (T) => Unit = { t => }, id: Option[ID] = None): Unit = _providers += new P(id, open, close)
  
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
  
  def using[R](f: => R): R = if (!has()) _using(list.get.get, f) else f
  def using[R](id: ID)(f: => R): R = if (!has(id)) _using(list.get(id).get, f) else f
  def using[R](open: => T, close: (T) => Unit = { t => }, optional: Boolean = false)(f: => R): R =
    if (optional && !has()) _using(new P(None, open, close), f) else f
  
  def has() = !_stack.isEmpty
  def has(id: ID) = !this(id).isEmpty
  
  def apply(): T = _stack.headOption.map(_._2).get
  def apply(id: ID): Option[T] = _stack.filter(_._1 == Some(id)).map(_._2).headOption
}



// version 2...
/*
object BufferHookB {
  def apply[T]() = new BufferHookB
}

class BufferHookB[I, O](name: String, reduce: (List[I], HookContext) => O) extends Hook[HookContext => I](name) {
  def register(v: HookContext => I)(implicit cx: ContextBuilder) = cx.register(this, v)
  def register(v: => I)(implicit cx: ContextBuilder) = cx.register(this, new Adapter(v).apply _)
  def register(v: I)(implicit cx: ContextBuilder) = cx.register(this, new Adapter(v).apply _)
  
  class Adapter(v: => I) { def apply(cx: HookContext) = v }
  
  def fragments(implicit cx: HookContext) = _get.map(v => v(cx))
  def apply()(implicit cx: HookContext) = reduce(fragments)
}

class BufferHookC[T](name: String, reduce: (List[T], HookContext) => T) extends BufferHookB[T, T](name, reduce) {
  
}*/