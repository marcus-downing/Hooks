package hooks

//import Hooks._
import Imports._
import scala.Product
import scala.collection.mutable.{ListBuffer}
import scala.reflect.{Manifest, ClassManifest}

/** Base class of all the hooks. You probably don't need to use this class directly.
  *
  * The `Hook` class provides the basic ability to store and retrieve behaviours of the given type
  * within the current context. New behaviour can only be added during the initialisation phase.
  *
  * For details, see individual hooks.
  * 
  */
  
abstract class Hook[S](val name: String) {
  /** Get all behaviours registered against this hook */
  def _get = HookContext { cx => cx.get(this) }
  /** Register a behaviour against this hook */
  def _register(s: S) = ContextBuilder { cb => cb.register(this, s) }
  def logErrors(f: => Unit) = try { f } catch { case x => x.printStackTrace }
}

/** Standalone hook base type. You do not need to use this class directly.
  *
  * Normally a hook can only be modified during the initilisation phase,
  * because that's the only time that a mutable context can be located.
  * Attempting to register behaviour with a hook at any other time
  * will throw an exception.
  * Standalone hooks bypass that restriction by wrapping the hook in a
  * permanently mutable context.
  *
  * @define standalone this is a standalone version of the hook that does not depend on a context.
  */
abstract class StandaloneHook[S](val base: Hook[S]) extends Hook[S](base.name) {
  val standaloneContext = HookContext.createDummy()
  /** Perform some task using a dummy global context */
  protected def standalone[R](f: => R): R = base.synchronized { standaloneContext.using { f } }
}



/**  A hook that collects objects of a given type.
  */
object ComponentHook {
  def apply[T](name: String) = new ComponentHook[T](name)
  
  object standalone {
    def apply[T](name: String) = new StandaloneComponentHook(new ComponentHook[T](name))
  }
}

/** A hook that collects objects of a given type.
  */
class ComponentHook[T](name: String) extends Hook[T](name) {
  def register(t: T): Unit = _register(t)
  def apply() = _get
  def components = _get
  def collect[S <: T] = _get.collect{ case s: S => s }
}


/** A hook that collects objects of a given type
  * 
  * @standalone
  */
class StandaloneComponentHook[T](base: ComponentHook[T]) extends StandaloneHook[T](base) {
  def register(t: T) = standalone { base.register(t) }
  def apply() = standalone { base() }
  def components = standalone { base.components }
  def collect[S <: T] = standalone { base.collect[S] }
}


/** A hook that fires actions.
  */
object ActionHook {  
  def simple(name: String) = new ActionHook0(name)
  def apply[A](name: String)(implicit d: D1) = new ActionHook[A](name)
  
  object standalone {
    //def simple(name: String) = new StandaloneActionHook0(new ActionHook0(name))
    def apply[A](name: String)(implicit d: D1) = new StandaloneActionHook[A](new ActionHook[A](name))
  }
}

/** A hook that fires actions.
  */

class ActionHook[S](name: String) extends Hook[S => Unit](name) {
  def register(fn: S => Unit): Unit = _register(fn)
  def register(fn: => Unit)(implicit d: D2): Unit = _register(new Adapter2(fn).apply _)
  
  class Adapter2(fn: => Unit) { def apply(s: S) { fn } }
  
  //def delegate[Q >: S](target: ActionHook[Q]) = actions += target.apply _
  
  def actions = _get
  def apply(s: S) { for (action <- actions) logErrors { action(s) } }
}

/** A hook that fires actions.
  * This is a special case that's simpler than a normal `ActionHook`: it takes no event type.
  */

class ActionHook0(name: String) extends ActionHook[Nil.type](name) {
  def apply() { apply(Nil) }
}

/** A hook that fires actions.
  * $standalone
  */
  
class StandaloneActionHook[S](base: ActionHook[S]) extends StandaloneHook(base) {
  def register(fn: S => Unit) = standalone { base.register(fn) }
  def register(fn: => Unit) = standalone { base.register(fn) }
  def actions = standalone { base.actions }
  def apply(s: S) = standalone { base(s) }
}

/*
class StandaloneActionHook0(base: ActionHook0) extends StandaloneActionHook(base) {
  def apply() = standalone { base.apply() }
}
*/


//  A hook that transforms a value
object FilterHook {
  def apply[V](name: String) = new FilterHook0[V](name)
  def apply[V, S](name: String)(implicit d: D1) = new FilterHook[V, S](name)
  
  object standalone {
    //def apply[V](name: String) = new StandaloneFilterHook0(new FilterHook0[V](name))
    def apply[V, S](name: String) = new StandaloneFilterHook(new FilterHook[V, S](name))
  }
}

class FilterHook[V, S](name: String) extends Hook[(V, S) => V](name) {
  def register(f: (V, S) => V): Unit = _register(f)
  def register(f: (V) => V): Unit = _register(new Adaptor3(f).filter _)
  
  //class Adaptor2(f: (V, S) => V) { def filter(v: V, s: S) = f(v,s) }
  class Adaptor3(f: (V) => V) { def filter(v: V, s: S) = f(v) }
  //class Adaptor4(f: (V, HookContext) => V) { def filter(v: V, s: S, cx: HookContext) = f(v, cx) }

  def filters = _get
  def apply(value: V, extra: S): V = filters.foldLeft(value) { (value, filter) => filter(value, extra) }
}

class FilterHook0[V](name: String) extends FilterHook[V, Nil.type](name: String) {
  def apply(value: V): V = apply(value, Nil)
}

class StandaloneFilterHook[V, S](base: FilterHook[V, S]) extends StandaloneHook(base) {
  def register(f: (V, S) => V) = standalone { base.register(f) }
  def register(f: (V) => V) = standalone { base.register(f) }
  def filters = standalone { base.filters }
  def apply(value: V, extra: S): V = standalone { base(value, extra) }
}

/*
class StandaloneFilterHook0[V](base: FilterHook0[V]) extends StandaloneFilterHook(base) {
  def apply(value: V) = standalone { base(value) }
}
*/


//  A hook that selects just one of the registered objects
object SelectableHook {
  def apply[M, T](name: String)(selector: (List[(M,T)]) => Option[T]) = new SelectableHook0(name, selector)
  def apply[M, T, S](name: String)(selector: (List[(M, T)], S) => Option[T]) = new SelectableHook(name, selector)
  
  object standalone {
    //def apply[M, T](name: String)(selector: (List[(M, T)]) => Option[T]) = new StandaloneSelectableHook0(new SelectableHook0[M, T](name, selector))
    def apply[M, T, S](name: String)(selector: (List[(M, T)], S) => Option[T]) = new StandaloneSelectableHook(new SelectableHook[M, T, S](name, selector))
  }
}

class SelectableHook[M, T, S](name: String, selector: (List[(M, T)], S) => Option[T]) extends Hook[(M, T)](name) {
  val guard = GuardHook[(M, T), S](name+" (guard)")
  def register(m: M)(t: T): Unit = _register((m, t))
  
  def items(extra: S): List[(M, T)] = guard(_get, extra).toList
  def apply(extra: S): Option[T] = selector(items(extra), extra)
}

class SelectableHook0[M, T](name: String, selector: (List[(M, T)]) => Option[T]) extends SelectableHook[M, T, Nil.type](name, new SelectableHook0Adaptor[M, T](selector).apply _) {
  def apply(): Option[T] = apply(Nil)
}

class SelectableHook0Adaptor[M, T](selector: List[(M, T)] => Option[T]) {
  def apply(items: List[(M, T)], nil: Nil.type): Option[T] = selector(items)
}

class StandaloneSelectableHook[M, T, S](base: SelectableHook[M, T, S]) extends StandaloneHook(base) {
  val guard = new StandaloneGuardHook(base.guard)
  def register(m: M)(t: T) = standalone { base.register(m)(t) }
  def items(extra: S) = standalone { base.items(extra) }
  def apply(extra: S) = standalone { base(extra) }
}

/*
class StandaloneSelectableHook0[M, T](base: SelectableHook0[M, T]) extends StandaloneSelectableHook(base) {
  def apply() = standalone { base() }
}
*/

/*
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
}*/


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
    def apply(name: String, prefix: String, infix: String, affix: String) = new StandaloneBufferHook(new BufferHook[String](name, prefix, infix, affix, strid))
    def apply(name: String, infix: String) = new StandaloneBufferHook(new BufferHook[String](name, "", infix, "", strid))
    def apply(name: String) = new StandaloneBufferHook(new BufferHook[String](name, "", "", "", strid))
  
    def apply[T](name: String, prefix: String, infix: String, affix: String, f: (T) => String) = new StandaloneBufferHook(new BufferHook[T](name, prefix, infix, affix, f))
    def apply[T](name: String, infix: String, f: (T) => String) = new StandaloneBufferHook(new BufferHook[T](name, "", infix, "", f))
    def apply[T](name: String, f: (T) => String) = new StandaloneBufferHook(new BufferHook[T](name, "", "", "", f))
  }
}

class BufferHook[T](name: String, prefix: String, infix: String, affix: String, fix: (T) => String) extends Hook[Function0[String]](name) {
  val earlyFilters = FilterHook[T](name+" (early filter)")
  val lateFilters = FilterHook[String](name+" (late filter)")

  def add(f: => T): Unit = _register(new Adaptor2(f).render _)
  def add(nested: BufferHook[_]): Unit = _register(new NestAdaptor(nested).render _)
  
  class Adaptor2(f: => T) { def render(): String = fix(earlyFilters(f)) }
  class NestAdaptor(nested: BufferHook[_]) { def render(): String = nested() }
  
  def fragments = _get
  def apply() = {
    val strings = fragments.map { (f: Function0[String]) => lateFilters(f()) }
    strings.mkString(prefix, infix, affix)
  }
}

class StandaloneBufferHook[T](base: BufferHook[T]) extends StandaloneHook(base) {
  def add(f: => T) = standalone { base.add(f) }
  def add(nested: BufferHook[_]) = standalone { base.add(nested) }
  def fragments = standalone { base.fragments }
  def apply = standalone { base.apply }
}

/*
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
*/

//  A hook that approves or rejects a value
object GuardHook {
  def apply[T](name: String) = new GuardHook0[T](name)
  def apply[T, S](name: String) = new GuardHook[T, S](name)
  
  object standalone {
    //def apply[T](name: String) = new StandaloneGuardHook0(new GuardHook0[T](name))
    def apply[T, S](name: String) = new StandaloneGuardHook(new GuardHook[T, S](name))
  }
}

class GuardHook[T, S](name: String) extends Hook[(T, S) => Boolean](name) {
  def register(f: (T, S) => Boolean)(implicit d: D1): Unit = _register(new Adaptor2(f).guard _)
  def register(f: (T) => Boolean)(implicit d: D2): Unit = _register(new Adaptor3(f).guard _)
  
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

class StandaloneGuardHook[T, S](base: GuardHook[T, S]) extends StandaloneHook(base) {
  def register(f: (T, S) => Boolean)(implicit d: D1) = standalone { base.register(f) }
  def register(f: (T) => Boolean)(implicit d: D2) = standalone { base.register(f) }
  
  def guards = standalone { base.guards }
  def apply(value: T, extra: S) = standalone { base(value, extra) }
  def apply(values: Seq[T], extra: S): Seq[T] = values.filter(v => this(v, extra))
  def apply(values: Option[T], extra: S): Option[T] = values.filter(v => this(v, extra))
  def apply(values: List[T], extra: S): List[T] = values.filter(v => this(v, extra))
}

/*
class StandaloneGuardHook0[T](base: GuardHook0[T]) extends StandaloneGuardHook(base) {
  def apply(value: T): Boolean = standalone { base(value) }
  def apply(values: Seq[T]): Seq[T] = values.filter(v => this(v))
  def apply(values: Option[T]): Option[T] = values.filter(v => this(v))
  def apply(values: List[T]): List[T] = values.filter(v => this(v))
}
*/


//  A hook that converts reversibly between two types, Inner and Outer
//  decode() produces an Outer value from an Inner
//  encode() combines the Outer value with an original Inner to produce a revised Inner
/*
class SimpleLensHook[I, O](name: String) extends Hook[Dec](name) {
  type Dec = (I) => O
  type Enc = (O, I) => I
  
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

object ConverterHook {
  //def apply[I, O](name: String) = new ConverterHook[I, O](name)
  // def standalone[I, O](name: String) = new StandaloneConverterHook(new ConverterHook[I, O](name))
  def precise[I,O](name: String) = new PreciseConverterHook[I, O](name)
}

/** A hook that converts values from one type to another.
  *
  * Register a variety of conversion functions against the hook,
  * and it will select the most appropriate one to use.
  *
  * This hook takes two type parameters: `I` represents the input type and `O` represents the output type.
  */
class ConverterHook[I, O](name: String) extends Hook[I => Option[O]](name) {
  def register[J <: I, Q <: O](f: J => Option[Q])(implicit mj: Manifest[J], mq: Manifest[Q]) = _register(new Adaptor(f, mj, mq).g _)
  class Adaptor[J <: I, Q <: O](f: J => Option[Q], mj: Manifest[J], mq: Manifest[Q]) {
    def g(i: I): Option[O] = if (mj >:> Manifest.singleType(i.asInstanceOf[AnyRef])) f(i.asInstanceOf[J]) else None
  }
  
  def apply(i: I): Option[O] = _get.view.flatMap(_(i)).headOption
  def apply(i: Option[I]): Option[O] = i.flatMap(j => apply(j))
}


class StandaloneConverterHook[I, O](base: ConverterHook[I, O]) extends StandaloneHook(base) {
  def register[J <: I, Q <: O](f: J => Option[Q])(implicit mj: Manifest[J], mq: Manifest[Q]) = standalone { base.register(f) }

  def apply(i: I) = standalone { base(i) }
  def apply(i: Option[I]) = standalone { base(i) }
}


class PreciseConverterHook[I, O](name: String) extends Hook[I => Option[O]](name) {
  def register(f: I => Option[O]) = _register(f)
  def apply(i: I): Option[O] = _get.view.flatMap(_(i)).headOption
  def apply(i: Option[I]): Option[O] = i.flatMap(j => apply(j))
}


/** A hook that converts values both ways between two types.
  *
  * Register a variety of encoder and decoder functions against the hook,
  * and it will select the most appropriate one to use.
  */
/*
class LensHook[I, O](name: String) extends Hook[Unit](name) {
  val dec = ConverterHook[I, O](name+" (dec)")
  val enc = ConverterHook[O, I](name+" (enc)")
  
  //def decode(o: O): Option[I] = 
  //def registerDecoder[J, Q](d: J => Option[Q])(implicit mj: Manifest[J], mq: Manifest[Q]) = dec.register(e)
  //def registerEncoder[Q, J](e: Q => Option[J])(implicit mq: Manifest[Q], mj: Manifest[J]) = enc.register(e)
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
  def providers = _get
  
  def register(provider: P): Unit = _register(provider)
  def register(open: => T, close: (T) => Unit = { t => }, id: Option[ID] = None): Unit = _register(new P(id, open, close))
  
  def apply() = _get.headOption
  def apply(id: ID) = _get.filter(_.id == Some(id)).headOption
  
  def _list = new ResourceProviderList[T, ID] {
    def all = providers
    def get = apply()
    def get(id: ID) = apply(id)
  }
  def tracker = new ResourceTracker(_list)
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

class BufferHookB[I, O](name: String, reduce: (List[I]) => O) extends Hook[ => I](name) {
  def register(v: => I) = _register(v)
  def register(v: I) = _register(new Adapter(v).apply _)
  
  class Adapter(v: => I) { def apply() = v }
  
  def fragments = _get.map(v => v())
  def apply() = reduce(fragments)
}

class BufferHookC[T](name: String, reduce: (List[T]) => T) extends BufferHookB[T, T](name, reduce) {
  
}
*/
