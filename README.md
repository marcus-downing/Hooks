**Hooks** provides a way for Scala programs to support plugins and optional features.

![Warning](http://www.minotaur.cc/warning.png)

**Under development**
The Hooks library is still being developed. Details will change before it's ready for release.

## Extend your code

Annotate your code with special hooks:

```scala
val nameFilter = FilterHook[String]()
val userSavedAction = ActionHook[User]()
```

Call into that hook in your program code:

```scala
val displayName = nameFilter(user.name)
userSavedAction(user)
```

Plugins can attach behaviours to that hook to modify it:

```scala
nameFilter.hook { name => name.toUpperCase }
userSavedAction.hook { user => sendNotificationEmail(user) }
```


## Optional features

Features group these modifications:

```scala
object Uppercase extends Feature("Uppercase") {
  def init() {
    nameFilter.hook { name => name.toUpperCase }
  }
}
```

Select which features you want to use when running code:

```scala
val features = List(Uppercase, AnotherFeature)
val displayName = FeatureRepository.using(features) {
  nameFilter(user.name)
}
```

Features can depend on each other and they'll be kept together:

```scala
object Uppercase extends Feature("Uppercase",
                                 depend = List(AnotherFeature)) {
```

You can control the order you want features to be initialised:

```scala
object Uppercase extends Feature("Uppercase",
                                 depend = List(AnotherFeature)
                                 before = List(AnotherFeature)) {
```


## Plugins

Load plugins from a directory:

```scala
val folder = new File("homedir/plugins")
val classpath = List(new File("myapplication.jar"))
new PluginLoader(folder, classpath, ".jar").registerAll()
```


## Read more
- **Start here: [Quick Start](https://github.com/marcusatbang/Hooks/wiki/Quick%20Start) &rarr;**

- [Introduction](https://github.com/marcusatbang/Hooks/wiki/Introduction)

- [Getting Started](https://github.com/marcusatbang/Hooks/wiki/Getting Started)

    - [`ActionHook`](https://github.com/marcusatbang/Hooks/wiki/ActionHook)
    - [`FilterHook`](https://github.com/marcusatbang/Hooks/wiki/FilterHook)
    - [`GuardHook`](https://github.com/marcusatbang/Hooks/wiki/GuardHook)
    - [`ComponentHook`](https://github.com/marcusatbang/Hooks/wiki/ComponentHook)
    - [`SelectableHook`](https://github.com/marcusatbang/Hooks/wiki/SelectableHook)
    - [`BufferHook`](https://github.com/marcusatbang/Hooks/wiki/BufferHook)
    - [`ResourceTrackerHook`](https://github.com/marcusatbang/Hooks/wiki/ResourceTrackerHook)

- [Advanced Topics](https://github.com/marcusatbang/Hooks/wiki/Advanced Topics)

- [Security](https://github.com/marcusatbang/Hooks/wiki/Security)

- [Examples](https://github.com/marcusatbang/Hooks/wiki/Examples)

    - [Basic application](https://github.com/marcusatbang/Hooks/wiki/Basic%20application)
    - [Website using the Play! framework](https://github.com/marcusatbang/Hooks/wiki/Play!%20framework) (not yet implemented)
    - [Loading plugins from a directory](https://github.com/marcusatbang/Hooks/wiki/Feature%20loader)