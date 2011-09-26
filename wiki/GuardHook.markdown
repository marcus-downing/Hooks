A guard hook approves or disapproves of values.
If no guard functions have been registered with the hook, it will approve of any value.
If more than one guard function has been registered, all must approve of a given value.

== How to use ==

Create a guard hook with the type of value to inspect:

    ```scala
    val babyNameGuard = GuardHook[String]("Baby names")
  ```

Register functions that check the value:

    ```scala
    babyNameGuard.register(name => name != "Fred")
  babyNameGuard.register(name => name != "Bill")
    ```

Check values against the guard to see if they're acceptable:

    ```scala
    val name = "Fred"
    if (babyNameGuard(name)) println("We're calling it "+name)
  else println("No we're not!")
    ```

You can also check an Option or List of values:

    ```scala
    val babyNames = List("John", "Mary", "Bill", "Sarah")
    val goodNames = babyNameGuard(babyNames)
    ```