![Timber](logo.png)

This is a logger with a small, extensible API which provides utility on top of Android's normal
`Log` class.

I copy this class into all the little apps I make. I'm tired of doing it. Now it's a library.

Behavior is added through `Tree` instances. You can install an instance by calling `Timber.plant`.
Installation of `Tree`s should be done as early as possible. The `onCreate` of your application is
the most logical choice.

The `DebugTree` implementation will automatically figure out from which class it's being called and
use that class name as its tag. Since the tags vary, it works really well when coupled with a log
reader like [Pidcat][1].

There are no `Tree` implementations installed by default because every time you log in production, a
puppy dies.


Usage
-----

Two easy steps:

 1. Install any `Tree` instances you want in the `onCreate` of your application class.
 2. Call `Timber`'s static methods everywhere throughout your app.

Check out the sample app in `timber-sample/` to see it in action.


Lint
----

Timber ships with embedded lint rules to detect problems in your app.

 *  **TimberArgCount** (Error) - Detects an incorrect number of arguments passed to a `Timber` call for
    the specified format string.

        Example.java:35: Error: Wrong argument count, format string Hello %s %s! requires 2 but format call supplies 1 [TimberArgCount]
            Timber.d("Hello %s %s!", firstName);
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 *  **TimberArgTypes** (Error) - Detects arguments which are of the wrong type for the specified format string.

        Example.java:35: Error: Wrong argument type for formatting argument '#0' in success = %b: conversion is 'b', received String (argument #2 in method call) [TimberArgTypes]
            Timber.d("success = %b", taskName);
                                     ~~~~~~~~
 *  **TimberTagLength** (Error) - Detects the use of tags which are longer than Android's maximum length of 23.

        Example.java:35: Error: The logging tag can be at most 23 characters, was 35 (TagNameThatIsReallyReallyReallyLong) [TimberTagLength]
            Timber.tag("TagNameThatIsReallyReallyReallyLong").d("Hello %s %s!", firstName, lastName);
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 *  **LogNotTimber** (Warning) - Detects usages of Android's `Log` that should be using `Timber`.

        Example.java:35: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            Log.d("Greeting", "Hello " + firstName + " " + lastName + "!");
                ~

 *  **StringFormatInTimber** (Warning) - Detects `String.format` used inside of a `Timber` call. Timber
    handles string formatting automatically.

        Example.java:35: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            Timber.d(String.format("Hello, %s %s", firstName, lastName));
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 *  **BinaryOperationInTimber** (Warning) - Detects string concatenation inside of a `Timber` call. Timber
    handles string formatting automatically and should be preferred over manual concatenation.

        Example.java:35: Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]
            Timber.d("Hello " + firstName + " " + lastName + "!");
                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 *  **TimberExceptionLogging** (Warning) - Detects the use of null or empty messages, or using the exception message
    when logging an exception.

        Example.java:35: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
             Timber.d(e, e.getMessage());
                         ~~~~~~~~~~~~~~


Download
--------

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'com.jakewharton.timber:timber:5.0.1'
}
```

Documentation is available at [jakewharton.github.io/timber/docs/5.x/](https://jakewharton.github.io/timber/docs/5.x/).

<details>
<summary>Snapshots of the development version are available in Sonatype's snapshots repository.</summary>
<p>

```groovy
repositories {
  mavenCentral()
  maven {
    url 'https://oss.sonatype.org/content/repositories/snapshots/'
  }
}

dependencies {
  implementation 'com.jakewharton.timber:timber:5.1.0-SNAPSHOT'
}
```

Snapshot documentation is available at [jakewharton.github.io/timber/docs/latest/](https://jakewharton.github.io/timber/docs/latest/).

</p>
</details>


License
-------

    Copyright 2013 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



 [1]: http://github.com/JakeWharton/pidcat/
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
