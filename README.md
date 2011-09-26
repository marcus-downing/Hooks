**Hooks** is a simple Scala library for handling plugins and modular functionality.

* Fits into any framework or platform
* Straightforward API
* Type-safe
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
    val buttonClicked = new ActionHook[Button]("Button Clicked")
    ```

3. Create a plugin that attaches a callback to that hook:

    ```scala
    object ButtonLogger extends Feature {
      def init (implicit val build: PluginContextBuilder) {
        buttonClicked.register(button => println("Somebody clicked "+button.name))
      }
    }
    ```

4. Register your plugin with the repository:

    ```scala
    PluginRepository.register(ButtonLogger)
    ```

5. Make a context with the desired plugins:

    ```scala
    val optionalFeatures = PluginRepository.optionalFeatures
    // ...choose some features...
    implicit val context = PluginRepository.makeContext(chosenFeatures)
    ```

6. In the appropriate place in client code, trigger that hook:

    ```scala
    buttonClicked(button)
    ```

## Introduction

As a program grows larger, it often faces a problem: one size no longer fits all.
A client may want to only enable the parts of a program that are relevant to them,
or they may want to add a feature nobody else would want.
If you fork the codebase into separate versions, it can become harder to maintain.
A better solution is to make the code modular.

The aim of **Hooks** is to handle, in as safe a manner as possible,
the uncertainty of optional and modular features.
It's inspired by the success of pluggable frameworks such as jQuery and WordPress,
but adds to that Scala's immutability and type safety.

A `Hook` is a point at which your program can be extended.
It connects code together, allowing some feature, resource or data
to be produced in one area of your code and consumed in another.
There are hooks for:

- Handling events ([`ActionHook`](https://github.com/marcusatbang/Hooks/wiki/ActionHook))

- Transforming and replacing values ([`FilterHook`](https://github.com/marcusatbang/Hooks/wiki/FilterHook))

- Permitting only certain values ([`GuardHook`](https://github.com/marcusatbang/Hooks/wiki/GuardHook))

- Collecting components ([`ComponentHook`](https://github.com/marcusatbang/Hooks/wiki/ComponentHook))

- Selecting one component from a list ([`SelectableHook`](https://github.com/marcusatbang/Hooks/wiki/SelectableHook))

- Tracking open resources ([`TrackerHook`](https://github.com/marcusatbang/Hooks/wiki/TrackerHook))

- Building complex string expressions ([`BufferHook`](https://github.com/marcusatbang/Hooks/wiki/BufferHook))

...and more. You can also build your own hook classes.

Each type of hook is typed to control the components registered with it and ensure their semantics.
This lets you open up your program to plugins without worrying that each one could break it.

**Hooks** provides tools to control the life cycle of plugins,
but does not impose a specific structure on your code.
It's simply a library that can be added to whatever framework your application uses.

## Read more
- **Start here: [Overview](https://github.com/marcusatbang/Hooks/wiki/Overview) &rarr;**

- [Getting Started](https://github.com/marcusatbang/Hooks/wiki/Getting Started)

- [Advanced User Guide](https://github.com/marcusatbang/Hooks/wiki/Advanced User Guide)

- [Security](https://github.com/marcusatbang/Hooks/wiki/Security)

- [Examples](https://github.com/marcusatbang/Hooks/wiki/Examples)
