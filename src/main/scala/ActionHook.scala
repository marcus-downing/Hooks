package hooks

import Imports._

/** A hook that fires actions.
  */
object ActionHook {
  def simple(name: String) = new ActionHook0(name)
  def apply[A](name: String)(implicit d: D1) = new ActionHook[A](name)

  object standalone {
    def simple(name: String) = new StandaloneActionHook0(new ActionHook0(name))
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

class StandaloneActionHook0(base: ActionHook0) extends StandaloneActionHook(base) {
  def apply() = standalone { base.apply() }
}


