Action hooks take action every time a certain event occures.

== How to use ==

Create an action hook:

    ```scala
    val eventHandler = ActionHook[MyEventType]("Event handler")
    ```

Attach an action to the hook:

    ```scala
    eventHandler.register { event => println(event.toString) }
    ```
  
When an event occurs, call the action:

    ```scala
    eventHandler(new MyEventType("Stuff"))
    ```

This will fire _all_ actions that were been registered against the action, in the order in which they were registered. If no actions have been registered against the hook, then firing it will do nothing.

== Side effects ==

Firing an action returns no result, throws no exceptions, and in theory it should have no side-effects at all.
In practice an action that doesn't have at least some effect would be meaningless.

== Actions vs Actors ==

An action hook resembles an actor in Scala or Akka:
when the relevant action occurs you fire the action hook, passing in a relevant event object (if applicable).
This fires any actions that have been registered against the hook.

The difference is that an action hook may fire the event to many handlers, or none.
Unlike an actor, an action hook does not run on a separate thread, nor can it return a value.

There are several different actor libraries for Scala, and your choice may depend on what other frameworks you are using.
For example, if you're writing a web application using Lift, you're likely to use Lift's actors.
Hooks therefore doesn't have ties to a single actor library.

=== Adding an event queue ===

The actions attached to a hook are fired immediately. In a running application this can have a number of down sides.

* The code that fired the action is paused until all the actions have finished.

* If an action fires other actions, you could end up in an infinite loop in which the original action never returns.

In this case we suggest you redirect actions through a central event queue.
Though not included by default, this is easy to implement using your actor library of choice.
The key 

== Error handling ==

To ensure the action hook can have no side effects on the containing code,
any exceptions thrown by an action are caught and redirected to an exception handler.

== Variants ==

There's a version of ActionHook which doesn't require an event object. Create one by omitting the event type parameter:

    ```scala
    val eventHandler = ActionHook("Event handler (without event object)")
    ```

There's also a version which isn't dependent on a `HookContext`. Create one with the `standalone` method:

    ```scala
    val eventHandler = ActionHook.standalone[MyEventType]("Event handler (standalone)")
    val eventHandler2 = ActionHook.standalone("Event handler (standalone, without event object)")
    ```

== Chaining action hooks ==

One action hook can call another. Hook them together with the `delegate` method:

    ```scala
    actionHook1.delegate(actionHook2)
    ```

Every time `actionHook1` fires, so will `actionHook2`.