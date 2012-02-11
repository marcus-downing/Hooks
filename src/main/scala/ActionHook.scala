package hooks

import Imports._

/** A hook that fires actions.
  */
object ActionHook {
  def simple() = new ActionHook0()
  def apply[A]() = new ActionHook[A]()

  object standalone {
    def simple() = new StandaloneActionHook0(new ActionHook0())
    def apply[A]() = new StandaloneActionHook[A](new ActionHook[A]())
  }
}

/** A hook that fires actions.
  */

class ActionHook[S]() extends Hook[S => Unit]() {
  def hook(fn: S => Unit): Unit = _register(fn)
  def hook(fn: => Unit)(implicit d: D2): Unit = _register(new Adapter2(fn).apply _)

  class Adapter2(fn: => Unit) { def apply(s: S) { fn } }

  //def delegate[Q >: S](target: ActionHook[Q]) = actions += target.apply _

  def actions = _get
  def apply(s: S) { for (action <- actions) logErrors { action(s) } }
}

/** A hook that fires actions.
  * This is a special case that's simpler than a normal `ActionHook`: it takes no event type.
  */

class ActionHook0() extends ActionHook[Nil.type]() {
  def apply() { apply(Nil) }
}

/** A hook that fires actions.
  * $standalone
  */

class StandaloneActionHook[S](base: ActionHook[S]) extends StandaloneHook(base) {
  def hook(fn: S => Unit) = standalone { base.hook(fn) }
  def hook(fn: => Unit) = standalone { base.hook(fn) }
  def actions = standalone { base.actions }
  def apply(s: S) = standalone { base(s) }
}

class StandaloneActionHook0(base: ActionHook0) extends StandaloneActionHook(base) {
  def apply() = standalone { base.apply() }
}


