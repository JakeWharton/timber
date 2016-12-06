Change Log
==========

Version 4.4.0 *(2016-12-06)*
----------------------------

 * New: `Tree.formatMessage` method allows customization of message formatting and rendering.
 * New: Lint checks ported to new IntelliJ PSI infrastructure.


Version 4.3.1 *(2016-09-19)*
----------------------------

 * New: Add convenience method `isLoggable` convenience method which also provides the tag.


Version 4.3.0 *(2016-08-18)*
----------------------------

 * New: Overloads for all log methods which accept only a `Throwable` without a message.


Version 4.2.0 *(2016-08-12)*
----------------------------

 * New: `Timber.plant` now has a varargs overload for planting multiple trees at once.
 * New: minSdkVersion is now 9 because reasons.
 * Fix: Consume explicitly specified tag even if the message is determined as not loggable (due to level).
 * Fix: Allow lint checks to run when `Timber.tag(..).v(..)`-style logging is used.


Version 4.1.2 *(2016-03-30)*
----------------------------

 * Fix: Tag-length lint check now only triggers on calls to `Timber`'s `tag` method. Previously it would
   match _any_ `tag` method and flag arguments longer than 23 characters.


Version 4.1.1 *(2016-02-19)*
----------------------------

 * New: Add method for retreiving the number of installed trees.


Version 4.1.0 *(2015-10-19)*
----------------------------

 * New: Consumer ProGuard rule automatically suppresses a warning for the use `@NonNls` on the 'message'
   argument for logging method. The warning was only for users running ProGuard and can safely be ignored.
 * New: Static `log` methods which accept a priority as a first argument makes dynamic logging at different
   levels easier to support.
 * Fix: Replace internal use of `Log.getStackTraceString` with our own implementation. This ensures that
   `UnknownHostException` errors are logged, which previously were suppressed.
 * Fix: 'BinaryOperationInTimber' lint rule now only triggers for string concatenation.


Version 4.0.1 *(2015-10-07)*
----------------------------

 * Fix: TimberArgTypes lint rule now allows booleans and numbers in '%s' format markers.
 * Fix: Lint rules now support running on Java 7 VMs.


Version 4.0.0 *(2015-10-07)*
----------------------------

 * New: Library is now an .aar! This means the lint rules are automatically applied to consuming
   projects.
 * New: `Tree.forest()` returns an immutable copy of all planted trees.
 * Fix: Ensure thread safety when logging and adding or removing trees concurrently.


Version 3.1.0 *(2015-05-11)*
----------------------------

 * New: `Tree.isLoggable` method allows a tree to determine whether a statement should be logged
   based on its priority. Defaults to logging all levels.


Version 3.0.2 *(2015-05-01)*
----------------------------

 * Fix: Strip multiple anonymous class markers (e.g., `$1$2`) from class names when `DebugTree`
   is creating an inferred tag.


Version 3.0.1 *(2015-04-17)*
----------------------------

 * Fix: String formatting is now always applied when arguments are present. Previously it would
   only trigger when an exception was included.


Version 3.0.0 *(2015-04-16)*
----------------------------

 * New: `Tree` and `DebugTree` APIs are much more extensible requiring only a single method to
   override.
 * New: `DebugTree` exposes `createStackElementTag` method for overriding to customize the
   reflection-based tag creation (for example, such as to add a line number).
 * WTF: Support for `wtf` log level.
 * `HollowTree` has been removed as it is no longer needed. Just extend `Tree`.
 * `TaggedTree` has been removed and its functionality folded into `Tree`. All `Tree` instances
   will receive any tags specified by a call to `tag`.
 * Fix: Multiple planted `DebugTree`s now each correctly received tags set from a call to `tag`.


Version 2.7.1 *(2015-02-17)*
----------------------------

 * Fix: Switch method of getting calling class to be consistent across API levels.


Version 2.7.0 *(2015-02-17)*
----------------------------

 * New: `DebugTree` subclasses can now override `logMessage` for access to the priority, tag, and
   entire message for every log.
 * Fix: Prevent overriding `Tree` and `TaggedTree` methods on `DebugTree`.


Version 2.6.0 *(2015-02-17)*
----------------------------

 * New: `DebugTree` subclasses can now override `createTag()` to specify log tags. `nextTag()` is
   also accessible for querying if an explicit tag was set.


Version 2.5.1 *(2015-01-19)*
----------------------------

 * Fix: Properly split lines which contain both newlines and are over 4000 characters.
 * Explicitly forbid `null` tree instances.


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
