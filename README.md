**Hooks** is a simple Scala library for handling plugins and modular functionality.

* Create type-safe 'hooks' representing ways your code can be extended
* Add behaviours to those hooks, from plugins or elsewhere
* Apply those hooks to invoke plugin code

Hooks has the following advantages:

* Fits into any framework or platform
* Straightforward and extensible API
* Scala's type safety ensures plugins behave themselves
* No configuration files or database
* No classloaders, code injection or other tricks

## Quick start
You can run the following commands in the Scala REPL.

1. Import the library:

    ```scala
    import hooks._
    ```

2. Create a hook:

    ```scala
    val nameFilter = FilterHook[String]("Name hook")
    ```

3. Create a feature that attaches a callback to that hook:

    ```scala
    object MyFeature extends Feature {
      def init (implicit val b: ContextBuilder) {
        nameFilter.register { name =>
          val words = name.split(" ").toList
          val words2 = (name.last+",") :: name.init
          words2.mkString(" ")
        }
      }
    }
    ```

4. Register your plugin with the repository:

    ```scala
    FeatureRepository.register(MyFeature)
    ```

5. Make a context with the desired features:

    ```scala
    val optionalFeatures = FeatureRepository.optionalFeatures
    // ...choose some features...
    implicit val context = FeatureRepository.makeContext(chosenFeatures)
    ```

6. In the appropriate place in client code, trigger that hook:

    ```scala
    val name = "John Smith"
    val displayName = nameFilter(name)
    ```
    
    This should result in the string: ```Smith, John```.

## Introduction

As a program grows larger, it often faces a problem: one size no longer fits all.
A client may want to only enable the parts of a program that are relevant to them,
or they may want to add a feature nobody else would want.
If you fork the codebase into separate versions, it can become harder to maintain.
A better solution is to make the code modular.

The aim of **Hooks** is to handle, in as safe a manner as possible,
the uncertainty of optional and modular features.
It's inspired by the success of pluggable architectures like jQuery and WordPress,
but adds a good dose of Scala's immutability and type safety.

A `Hook` is a point at which your program can be extended.
It connects code together, allowing some feature, resource or data
to be produced in one area of your code and consumed in another.
There are hooks for:

- Handling events ([`ActionHook`](https://github.com/marcusatbang/Hooks/wiki/ActionHook))

- Transforming and replacing values ([`FilterHook`](https://github.com/marcusatbang/Hooks/wiki/FilterHook))

- Permitting only certain values ([`GuardHook`](https://github.com/marcusatbang/Hooks/wiki/GuardHook))

- Collecting components ([`ComponentHook`](https://github.com/marcusatbang/Hooks/wiki/ComponentHook))

- Selecting one component from a list ([`SelectableHook`](https://github.com/marcusatbang/Hooks/wiki/SelectableHook))

- Tracking open resources ([`ResourceTrackerHook`](https://github.com/marcusatbang/Hooks/wiki/ResourceTrackerHook))

- Building complex expressions ([`BufferHook`](https://github.com/marcusatbang/Hooks/wiki/BufferHook))

...and more. You can also build your own hook classes.

Each type of hook is typed to control the components registered with it and ensure their semantics.
This lets you open up your program to plugins without worrying that each one could break it.

**Hooks** provides tools to control the life cycle of plugins and features,
but does not impose a specific structure on your code.
It's simply a library that can be added to whatever framework your application uses.

## Read more
- **Start here: [Overview](https://github.com/marcusatbang/Hooks/wiki/Overview) &rarr;**

- [Getting Started](https://github.com/marcusatbang/Hooks/wiki/Getting Started)

- [Advanced User Guide](https://github.com/marcusatbang/Hooks/wiki/Advanced User Guide)

- [Security](https://github.com/marcusatbang/Hooks/wiki/Security)

- [Examples](https://github.com/marcusatbang/Hooks/wiki/Examples)
