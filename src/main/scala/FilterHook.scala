package hooks

import Imports._

//  A hook that transforms a value
object FilterHook {
  def apply[V](name: String) = new FilterHook0[V](name)
  def apply[V, S](name: String)(implicit d: D1) = new FilterHook[V, S](name)

  object standalone {
    def apply[V](name: String) = new StandaloneFilterHook0(new FilterHook0[V](name))
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


class StandaloneFilterHook0[V](base: FilterHook0[V]) extends StandaloneFilterHook(base) {
  def apply(value: V) = standalone { base(value) }
}


