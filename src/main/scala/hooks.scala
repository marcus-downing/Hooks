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
  
abstract class Hook[S]() {
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
  * @define standalone This is a standalone version of the hook that does not depend on a context.
  */
abstract class StandaloneHook[S](val base: Hook[S]) extends Hook[S](base.) {
  val standaloneContext = HookContext.createDummy()
  /** Perform some task using a dummy global context */
  protected def standalone[R](f: => R): R = base.synchronized { standaloneContext.using { f } }
}


//  A hook that collects fragments and assembles them into one string
object BufferHook {
  def strid(v: String) = v
  
  def apply(prefix: String, infix: String, affix: String) = new BufferHook[String](prefix, infix, affix, strid)
  def apply(infix: String) = new BufferHook[String]("", infix, "", strid)
  def apply() = new BufferHook[String]("", "", "", strid)
  
  def apply[T](prefix: String, infix: String, affix: String, f: (T) => String) = new BufferHook[T](prefix, infix, affix, f)
  def apply[T](infix: String, f: (T) => String) = new BufferHook[T]("", infix, "", f)
  def apply[T](f: (T) => String) = new BufferHook[T]("", "", "", f)
  
  object standalone {
    def apply(prefix: String, infix: String, affix: String) = new StandaloneBufferHook(new BufferHook[String](prefix, infix, affix, strid))
    def apply(infix: String) = new StandaloneBufferHook(new BufferHook[String]("", infix, "", strid))
    def apply() = new StandaloneBufferHook(new BufferHook[String]("", "", "", strid))
  
    def apply[T](prefix: String, infix: String, affix: String, f: (T) => String) = new StandaloneBufferHook(new BufferHook[T](prefix, infix, affix, f))
    def apply[T](infix: String, f: (T) => String) = new StandaloneBufferHook(new BufferHook[T]("", infix, "", f))
    def apply[T](f: (T) => String) = new StandaloneBufferHook(new BufferHook[T]("", "", "", f))
  }
}

class BufferHook[T](prefix: String, infix: String, affix: String, fix: (T) => String) extends Hook[Function0[String]]() {
  val earlyFilters = FilterHook[T](+" (early filter)")
  val lateFilters = FilterHook[String](+" (late filter)")

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
class StandaloneBufferHook[T](prefix: String, infix: String, affix: String, fix: (T) => String) extends Hook[Function0[String]]() {
  val _producers = new ListBuffer[() => String]()
  val earlyFilters = FilterHook.standalone[T](+" (early filter)")
  val lateFilters = FilterHook.standalone[String](+" (late filter)")

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

//  A hook that converts reversibly between two types, Inner and Outer
//  decode() produces an Outer value from an Inner
//  encode() combines the Outer value with an original Inner to produce a revised Inner
/*
class SimpleLensHook[I, O]() extends Hook[Dec]() {
  type Dec = (I) => O
  type Enc = (O, I) => I
  
  class Counterpart extends Hook[Enc]() {
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

class LensHook[I, O, S]() extends Hook[Dec]() {
  type Dec = (I, S) => (HookContext) => O
  type Enc = (O, I, S) => (HookContext) => I
  
  class Counterpart extends Hook[Enc]() {
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
  //def apply[I, O]() = new ConverterHook[I, O]()
  // def standalone[I, O]() = new StandaloneConverterHook(new ConverterHook[I, O]())
  def precise[I,O]() = new PreciseConverterHook[I, O]()
}

/** A hook that converts values from one type to another.
  *
  * Register a variety of conversion functions against the hook,
  * and it will select the most appropriate one to use.
  *
  * This hook takes two type parameters: `I` represents the input type and `O` represents the output type.
  */
class ConverterHook[I, O]() extends Hook[I => Option[O]]() {
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


class PreciseConverterHook[I, O]() extends Hook[I => Option[O]]() {
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
class LensHook[I, O]() extends Hook[Unit]() {
  val dec = ConverterHook[I, O](+" (dec)")
  val enc = ConverterHook[O, I](+" (enc)")
  
  //def decode(o: O): Option[I] = 
  //def registerDecoder[J, Q](d: J => Option[Q])(implicit mj: Manifest[J], mq: Manifest[Q]) = dec.register(e)
  //def registerEncoder[Q, J](e: Q => Option[J])(implicit mq: Manifest[Q], mj: Manifest[J]) = enc.register(e)
}
*/



object ResourceTrackerHook {
  def apply[T, ID]() = new ResourceTrackerHook[T, ID]()
  object standalone {
    def apply[T, ID]() = new StandaloneResourceTrackerHook[T, ID]()
  }
}

class ResourceProvider[T, ID](val id: Option[ID], open_fn: => T, val close: (T) => Unit) {
  def open() = open_fn
}

class ResourceTrackerHook[T, ID]() extends Hook[ResourceProvider[T, ID]]() {
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

class StandaloneResourceTrackerHook[T, ID]() extends Hook[ResourceProvider[T, ID]]() {
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

class BufferHookB[I, O](reduce: (List[I]) => O) extends Hook[ => I]() {
  def register(v: => I) = _register(v)
  def register(v: I) = _register(new Adapter(v).apply _)
  
  class Adapter(v: => I) { def apply() = v }
  
  def fragments = _get.map(v => v())
  def apply() = reduce(fragments)
}

class BufferHookC[T](reduce: (List[T]) => T) extends BufferHookB[T, T](reduce) {
  
}
*/
