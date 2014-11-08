Change Log
==========

Version 2.5.0 *(2014-11-08)*
----------------------------

 * New: `Timber.asTree()` exposes functionality as a `Tree` instance rather than static methods.


Version 2.4.2 *(2014-11-07)*
----------------------------

 * Eliminate heap allocation when dispatching log calls.


Version 2.4.1 *(2014-06-19)*
----------------------------

 * Fix: Calls with no message but a `Throwable` are now correctly logged.


Version 2.4.0 *(2014-06-10)*
----------------------------

 * New: `uproot` and `uprootAll` methods allow removing trees.


Version 2.3.0 *(2014-05-21)*
----------------------------

 * New: Messages longer than 4000 characters will be split into multiple lines.


Version 2.2.2 *(2014-02-12)*
----------------------------

 * Fix: Include debug level in previous fix which avoids formatting messages with no arguments.


Version 2.2.1 *(2014-02-11)*
----------------------------

 * Fix: Do not attempt to format log messages which do not have arguments.


Version 2.2.0 *(2014-02-02)*
----------------------------

 * New: verbose log level added (`v()`).
 * New: `timber-lint` module adds lint check to ensure you are calling `Timber` and not `Log`.
 * Fix: Specifying custom tags is now thread-safe.


Version 2.1.0 *(2013-11-21)*
----------------------------

 * New: `tag` method allows specifying custom one-time tag. Redux!


Version 2.0.0 *(2013-10-21)*
----------------------------

 * Logging API is now exposed as static methods on `Timber`. Behavior is added by installing `Tree`
   instances for logging.


Version 1.1.0 *(2013-07-22)*
----------------------------

 * New: `tag` method allows specifying custom one-time tag.
 * Fix: Exception-containing methods now log at the correct level.


Version 1.0.0 *(2013-07-17)*
----------------------------

Initial cut. (Get it?)
