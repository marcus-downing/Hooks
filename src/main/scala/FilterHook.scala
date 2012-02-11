package hooks

import Imports._

//  A hook that transforms a value
object FilterHook {
  def apply[V]() = new FilterHook0[V]()
  def apply[V, S]() = new FilterHook[V, S]()

  object standalone {
    def apply[V]() = new StandaloneFilterHook0(new FilterHook0[V]())
    def apply[V, S]() = new StandaloneFilterHook(new FilterHook[V, S]())
  }
}

class FilterHook[V, S]() extends Hook[(V, S) => V]() {
  def hook(f: (V, S) => V): Unit = _register(f)
  def hook(f: (V) => V): Unit = _register(new Adaptor3(f).filter _)

  //class Adaptor2(f: (V, S) => V) { def filter(v: V, s: S) = f(v,s) }
  class Adaptor3(f: (V) => V) { def filter(v: V, s: S) = f(v) }
  //class Adaptor4(f: (V, HookContext) => V) { def filter(v: V, s: S, cx: HookContext) = f(v, cx) }

  def filters = _get
  def apply(value: V, extra: S): V = filters.foldLeft(value) { (value, filter) => filter(value, extra) }
}

class FilterHook0[V]() extends FilterHook[V, Nil.type]() {
  def apply(value: V): V = apply(value, Nil)
}

class StandaloneFilterHook[V, S](base: FilterHook[V, S]) extends StandaloneHook(base) {
  def hook(f: (V, S) => V) = standalone { base.register(f) }
  def hook(f: (V) => V) = standalone { base.register(f) }
  def filters = standalone { base.filters }
  def apply(value: V, extra: S): V = standalone { base(value, extra) }
}


class StandaloneFilterHook0[V](base: FilterHook0[V]) extends StandaloneFilterHook(base) {
  def apply(value: V) = standalone { base(value) }
}


