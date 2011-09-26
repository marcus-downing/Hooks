A resource tracker hook implements the loan pattern for tracking resources, such as files or database connections, to prevent them "leaking".
A resource is only open for as long as it takes to execute the callback function, at the end of which it is closed.

There are two ways of providing a resource:
you can register the means of opening and closing the resource when the tracker in initialised,
or you can open a resource directly when you need it.

== How to use ==

Create a tracker hook:

    ```scala
    val hook = Resource TrackerHook[Connection]("Database connection")
    ```
    
Give it a way of opening a resource:

    ```scala
    hook.register(openConnection())
    ```

Create a tracker based on this hook:

    ```scala
    val connectionTracker = hook.tracker
    ```
    
    Unlike the hook, the tracker itself is tied to the current thread you're using.

Assuming a resource has been opened, you can retrieve it with the `apply` method:

    ```scala
    val connection = connectionTracker()
    ```

==== Registering a resource provider ====

Call `register` at plugin initialisation time, providing the means of opening a resource:

    ```scala
    hook.register(openConnection())
    ```

This is a function parameter, so you can define it elsewhere:

    ```scala
    def open = { openConnection() }
    connectionTrackerHook.register(open)
    ```

Often a resource will need some method to safely close it:

    ```scala
    connectionTracker.register(openConnection(), _.close())
    ```

Whenever a resource is needed, call `using` and the tracker will open the resource for you:

    ```scala
    connectionTracker.using {
      ...do stuff...
    }
    ```

The `using` method won't open a resource unless one is needed, so it's safe to call it whenever you need to ensure that a resource is open.
The following will only open one connect:

    ```scala
    connectionTracker.using {
      connectionTracker.using {
        connectionTracker.using {
          ...do stuff...
        }
      }
    }
    ```

==== Giving a resource directly ====

Call `using` to provide an `open` and `close` function:

    ```scala
    connectionTracker.using(openConnection(), _.close()) {
      ...do stuff...
    }
    ```

This will take priority over any registered functions, so the following will only open one connection:

    ```scala
    connectionTracker.using(openConnection(), _.close()) {
      connectionTracker.using {
        ...do stuff...
      }
    }
    ```

But this *will* open a resource even if one is already open, so the following will open two connections:

    ```scala
    connectionTracker.using {
      connectionTracker.using(openConnection(), _.close()) {
        ...do stuff...
      }
    }

You can change this behaviour with the `optional` parameter:

    ```scala
    connectionTracker.using {
      connectionTracker.using(openConnection(), _.close(), optional = true) {
        ...do stuff...
      }
    }


== Resource stack ==

If you call `using` in a way which opens two resources, the tracker will keep a stack of the open resources.
Calling `apply` will give you the most recently opened one.

    ```scala
    val nameTracker = TrackerHook[String]("User name tracker")
    nameTracker.using("John") {
      println("I am "+nameTracker())      // prints "I am John"
      nameTracker.using("Bob") {
        println("I am "+nameTracker())    // prints "I am Bob"
        nameTracker.using("Joe") {
          println("I am "+nameTracker())  // prints "I am Joe"
        }
        println("I am "+nameTracker())    // prints "I am Bob"
      }
      println("I am "+nameTracker())      // prints "I am John"
    }
    ```

You may only wish to open a single resource at once.
You can use the `optional` argument to `using` to only open a resource if there isn't one already open.

    ```scala
    val nameTracker = TrackerHook[String]("User name tracker")
    nameTracker.using("John") {
      nameTracker.using("Bob", optional = true) {
        println("I am "+nameTracker())    // prints "I am John"
      }
    }
    ```

=== Finding resources in the stack ===

Sometimes not all resources are equal, and the resource you want isn't the one at the top of the stack.
You can find a resource in the stack by its type, by an identifier or by a function.

All these methods return an Option of the resource type.

==== Finding a resource by type ====

If your tracker can contain resources of more than one type, you can find the most recent resource of a given type:

    ```scala
    class Bar extends Foo
    ...
    fooHook.with(new Bar) {
      fooHook.with(new Foo) {
        val bar = fooHook[Bar]()
      }
    }
    ```
    
==== Finding a resource by identifier ====

When you create a hook, you can tell it to expect an identifier for each item.

    ```scala
    val connectionHook = TrackerHook[Connection, String]("Database connection")
    connectionHook.with(openLocalConnection, "local", closeConnection) {
      connectionHook.with(openRemoteConnection, "remote", closeConnection) {
        val localConnection = connectionHook.get("local")
        ...
      }
    }
    ```

==== Finding a resource with a function ====

Finally you can pass in a function to select the value you wish. This can either return a boolean for each item, or it can select one element from a list, with or without an ID.

    ```scala
    fooHook.get(foo: Foo => f.isReady)
    fooHook.get((id: String, f: Foo) => id != "special" && f.isReady)
    fooHook.get(fs: List[Foo] => fs.head)
    fooHook.get(fs: List[(ID, Foo)] => fs.head._2)
    ```

== Threading, actors and state ==

Typically a hook stores no state.

The resource tracker hook has state.




Other hooks are immutable, but a resource tracker hook has state.

That makes its threading more complicated: if you pass it between threads, or between actors, there is a danger of inconsistent behaviour. While the registration of 

If you anticipate.



