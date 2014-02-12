Change Log
==========

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
