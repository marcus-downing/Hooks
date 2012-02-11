package hooks

import Imports._

/**  A hook that collects objects of a given type.
  */
object ComponentHook {
  def apply[T]() = new ComponentHook[T]()

  object standalone {
    def apply[T]() = new StandaloneComponentHook(new ComponentHook[T]())
  }
}

/** A hook that collects objects of a given type.
  */
class ComponentHook[T]() extends Hook[T]() {
  def hook(t: T): Unit = _register(t)
  def apply() = _get
  def components = _get
  def collect[S <: T](implicit m: Manifest[S]) = _get.collect{ case s: S => s }
}

/** A hook that collects objects of a given type
  * 
  * @standalone
  */
class StandaloneComponentHook[T](base: ComponentHook[T]) extends StandaloneHook[T](base) {
  def hook(t: T) = standalone { base.hook(t) }
  def apply() = standalone { base() }
  def components = standalone { base.components }
  def collect[S <: T](implicit m: Manifest[S]) = standalone { base.collect[S] }
}

