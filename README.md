Hooks is a simple Scala library for handling plugins.

* Self-contained library that can be used with any framework or platform
* Straightforward API
* Type-safe
* No configuration files or database
* No classloaders, code injection, attributes or other tricks

## How to use it
1. Create a hook:

        val buttonClicked = new ActionHook[Button]("Button Clicked")

2. Create a plugin that attaches a callback to that hook:

        object ButtonLogger extends Feature {
          def init (implicit val build: PluginContextBuilder) {
            buttonClicked.register(button => println("Somebody clicked "+button.name))
          }
        }

3. Register your plugin with the repository

        PluginRepository.register(ButtonLogger)

4. Make a context with the currently desired plugins

        val optionalFeatures = PluginRepository.optionalFeatures
        // ...choose active features...
        implicit val context = PluginRepository.makeContext(chosenFeatures)

5. In the appropriate place in client code, trigger that hook:

        def onClick(button: Button){
          buttonClicked(button)
        }

## Read more
- Start here: [Overview](Hooks/wiki/Overview) &rarr;

- [User Guide](Hooks/wiki/User Guide)

- [Advanced User Guide](Hooks/wiki/Advanced User Guide)

- [Examples](Hooks/wiki/Examples)
