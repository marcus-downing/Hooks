package hooks

import Imports._

//  A hook that selects just one of the registered objects
object SelectableHook {
  def apply[M, T](selector: (List[(M,T)]) => Option[T]) = new SelectableHook0(selector)
  def apply[M, T, S](selector: (List[(M, T)], S) => Option[T]) = new SelectableHook(selector)

  object standalone {
    def apply[M, T](selector: (List[(M, T)]) => Option[T]) = new StandaloneSelectableHook0(new SelectableHook0(selector))
    def apply[M, T, S](selector: (List[(M, T)], S) => Option[T]) = new StandaloneSelectableHook(new SelectableHook(selector))
  }
}

class SelectableHook[M, T, S](selector: (List[(M, T)], S) => Option[T]) extends Hook[(M, T)]() {
  val guard = GuardHook[(M, T), S]()
  def hook(m: M)(t: T): Unit = _register((m, t))

  def items(extra: S): List[(M, T)] = guard(_get, extra).toList
  def apply(extra: S): Option[T] = selector(items(extra), extra)
}

class SelectableHook0[M, T](selector: (List[(M, T)]) => Option[T]) extends SelectableHook[M, T, Nil.type](new SelectableHook0Adaptor[M, T](selector).apply _) {
  override val guard = GuardHook[(M, T)](+" (guard)")
  def apply(): Option[T] = apply(Nil)
}

class SelectableHook0Adaptor[M, T](selector: List[(M, T)] => Option[T]) {
  def apply(items: List[(M, T)], nil: Nil.type): Option[T] = selector(items)
}

class StandaloneSelectableHook[M, T, S](base: SelectableHook[M, T, S]) extends StandaloneHook(base) {
  val guard = new StandaloneGuardHook(base.guard)
  def hook(m: M)(t: T) = standalone { base.hook(m)(t) }
  def items(extra: S) = standalone { base.items(extra) }
  def apply(extra: S) = standalone { base(extra) }
}

class StandaloneSelectableHook0[M, T](base: SelectableHook0[M, T]) extends StandaloneSelectableHook(base) {
  override val guard = new StandaloneGuardHook0(base.guard)
  def apply(): Option[T] = standalone { base() }
}
