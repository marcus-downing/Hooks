Hooks is a simple Scala library for handling plugins. It aims to be straightforward, unintrusive and type-safe.

## How to use it
1. Create a hook:

        val buttonClicked = new ActionHook[Button]("Button Clicked")

2. Create a plugin that attached something to that hook:

        object MyFeature extends Feature {
          buttonClicked.register(button => println("Somebody clicked "+button.name))
        }

3. Register your plugin, and make a context with the currently desired plugins:

        PluginRepository.registerFeatures(MyFeature)
        val optionalFeatures = PluginRepository.optionalFeatures
        ...
        implicit val context = PluginRepository.makeContext(chosenFeatures)

4. In the appropriate place in client code, trigger that hook:

        def onClick(button: Button){
          buttonClicked(button)
        }
