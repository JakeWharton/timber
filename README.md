Timber
======

This is a logger with a small API, tag inference, and is injection friendly. Since the tags vary,
it works really well when coupled with a log reader like [Pidcat][1].

I copy this class into all the little apps I make. I'm tired of doing it. Now it's a library.

It has two implementations: `Timber.DEBUG` and `Timber.PROD`.

The debug implementation will automatically figure out which class it's being called from and use
that as its tag. The API also does string formatting for you.

The production implementation does nothing. Like, nothing. Every time you log in production, a
puppy dies.


Usage
-----

Two easy steps:

 1. Figure out if you want `Timber.DEBUG` or `Timber.PROD` and bind it.
 2. Inject an instance of `Timber` everywhere you want to log.

See the sample app in `timber-sample/` to see it in action.


Download
--------

Download [the latest JAR][2] or grab via Maven:

```xml
<dependency>
  <groupId>com.jakewharton.timber</groupId>
  <artifactId>timber</artifactId>
  <version>(insert latest version)</version>
</dependency>
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
