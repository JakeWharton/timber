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


Download
--------

Download [the latest JAR][2] or grab via Maven:

```xml
<dependency>
  <groupId>com.jakewharton.timber</groupId>
  <artifactId>timber</artifactId>
  <version>2.5.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.jakewharton.timber:timber:2.5.0'
```


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
 [2]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.jakewharton.timber&a=timber&v=LATEST
 [3]: http://square.github.io/dagger/
 [4]: http://jakewharton.github.io/butterknife/
