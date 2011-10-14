A selectable hook resembles a [component hook][ComponentHook], but only returns a simple item from those registered.
When you create the hook, you pass in a function which selects

== How to use ==

Create a selectable hook, passing in a selector:

    ```scala
    val versionsHook = SelectableHook[String]("Versions")(selectVersion)
    ```

This selector should be a function to choose one of a list of versions:

    ```scala
    def selectVersion(version: List[String], c: HookContext): String = versions.sort.head
    ```
  
Register a number of components:

    ```scala
    versionsHook.register("2.8.0")
    versionsHook.register("2.8.1")
    versionsHook.register("2.9.0")
    versionsHook.register("2.9.1")
    ```

Collect registered values later:

    ```scala
    val version = verionsHook()
    ```

== Guard ==

Every selectable hook comes with a guard hook. You can register functions on this guard hook to control which items are passed to the selector.

    ```scala
    versionsHook.guard.register(version => version != "2.9.0")
    ```

This can be used to restrict the list to only include valid items, before they are passed to the selector.

== Extra infomation ==

You don't necessarily want to select the same item all the time; your choice may be dependent on circumstances. You can effect this by passing in an extra parameter:

    ```scala
    val versionsHook = SelectableHook[String, String]("Versions for package")(selectVersion)
    ```

The selector function will then be passed this extra parameter:

    ```scala
    def selectVersion(version: List[String], package: String, c: HookContext): String = ...
    ```

The guard will also be passed the same information:

    ```scala
    versionsHook.guard.register((version, package) => version != "2.9.0")
    ```

== Variants ==

