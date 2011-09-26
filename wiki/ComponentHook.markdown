Component hooks collect objects of a given type.

== How to use ==

Create a component hook:

    ```scala
    val menuItems = ComponentHook[MenuItem]("Menu item hook")
    ```

Register a component:

    ```scala
    menuItems.register(exitMenuItem)
    ```

Collect registered values later:

    ```scala
    val items = menuItems()
    ```

If a component hook's type is general, you can retrieve only items of a more specific type using the `collect` method:

    ```scala
    val items = menuItems.get[SpecialMenuItem]
    ```