Filters transform or replace a given value.
When a value is fed through a filter hook, each registered filter is applied (in the order they were registered) to produce the final value.

== How to use ==

Create a filter hook:

    ```scala
    val nameFilter = FilterHook[String]("Name hook")
    ```

Attach a filter that transforms the value

    ```scala
    nameFilter.register { name => "Mr "+name }
    ```

Feed a value through the filter:

    ```scala
    val name2 = nameFilter(name)
    ```

You can feed many values through a filter at once:

    ```scala
    val clientNames2 = clientNames.map(nameFilter)
  ```

== Extra parameter ==

As well as the value to be transformed, a you can supply extra information when you call into a filter. This value doesn't change from the application of one filter to the next.

    ```scala
    val nameFilter = FilterHook[String, String]("Name hook with title")
    ```

Attach a function to the hook that takes both the value, and the extra information:

    ```scala
    nameFilter.register { (name, title) => title+" "+name }
    ````

Feed a value through the filter, along with the extra information:

    ```scala
    val name2 = nameFilter(name, title)
    ```

If you need to pass more than one value as your extra parameter, you can use a tuple:

    ```scala
    val nameFilter = FilterHook[String, (String, String, String)]("Name with title, forename and surname")
    nameFilter.register { (name, (title, forename, surname)) => title+" "+name }
    val name2 = nameFilter(name, (title, forename, surname))
    ```

