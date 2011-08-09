Hooks is a simple Scala library for handling plugins. It aims to be straightforward, unintrusive and type-safe.

## How to use it
1. Create a hook:

        val buttonClicked = new ActionHook[Button]("Button Clicked")

2. Create a plugin that attaches a callback to that hook:

        object ButtonLogger extends Feature {
          def logButtonClick(button: Button)(implicit val c: PluginContext) =
            println("Somebody clicked "+button.name)
          def init (implicit val build: PluginContextBuilder) {
            buttonClicked.register(logButtonClick)
          }
        }

3. Register your plugin, and make a context with the currently desired plugins:

        PluginRepository.registerFeatures(ButtonLogger)
        val optionalFeatures = PluginRepository.optionalFeatures
        // ...choose active features...
        implicit val context = PluginRepository.makeContext(chosenFeatures)

4. In the appropriate place in client code, trigger that hook:

        def onClick(button: Button){
          buttonClicked(button)
        }

## Read more
- [Overview](Hooks/wiki/Overview)

- [Examples](Hooks/wiki/Examples)
