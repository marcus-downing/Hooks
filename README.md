Hooks is a simple Scala library for handling plugins. It aims to be straightforward, unintrusive and type-safe.

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
- [Overview](Hooks/wiki/Overview)

- [Examples](Hooks/wiki/Examples)
