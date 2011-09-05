**Hooks** is a simple Scala library for handling plugins and modular functionality.

* Self-contained library that can be used with any framework or platform
* Straightforward API
* Type-safe
* No configuration files or database
* No classloaders, code injection or other tricks

## How to use it
1. Create a hook:

    ```scala
    val buttonClicked = new ActionHook[Button]("Button Clicked")
    ```

2. Create a plugin that attaches a callback to that hook:

    ```scala
    object ButtonLogger extends Feature {
      def init (implicit val build: PluginContextBuilder) {
        buttonClicked.register(button => println("Somebody clicked "+button.name))
      }
    }
    ```

3. Register your plugin with the repository:

    ```scala
    PluginRepository.register(ButtonLogger)
    ```

4. Make a context with the desired plugins:

    ```scala
    val optionalFeatures = PluginRepository.optionalFeatures
    // ...choose some features...
    implicit val context = PluginRepository.makeContext(chosenFeatures)
    ```

5. In the appropriate place in client code, trigger that hook:

    ```scala
    buttonClicked(button)
    ```

## Read more
- **Start here: [Overview](Hooks/wiki/Overview) &rarr;**

- [Getting Started](Hooks/wiki/Getting Started)

- [Advanced User Guide](Hooks/wiki/Advanced User Guide)

- [Security](Hooks/wiki/Security)

- [Examples](Hooks/wiki/Examples)
