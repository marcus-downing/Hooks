package hooks

import Imports._

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
  def collect[S <: T](implicit m: Manifest[S]) = _get.collect{ case s: S => s }
}

/** A hook that collects objects of a given type
  * 
  * @standalone
  */
class StandaloneComponentHook[T](base: ComponentHook[T]) extends StandaloneHook[T](base) {
  def register(t: T) = standalone { base.register(t) }
  def apply() = standalone { base() }
  def components = standalone { base.components }
  def collect[S <: T](implicit m: Manifest[S]) = standalone { base.collect[S] }
}

