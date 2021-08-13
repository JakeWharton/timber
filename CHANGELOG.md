# Change log

## [Unreleased]


## [5.0.1] - 2021-08-13

### Fixed

- Fix TimberArgCount lint check false positive on some calls to `String.format`.


## [5.0.0] - 2021-08-10

The library has been rewritten in Kotlin, but it remains binary-compatible with 4.x.
The intent is to support Kotlin multiplatform in the future.
This is otherwise a relatively minor, bug-fix release.

### Changed

- Minimum supported API level is now 14.
- Minimum supported AGP (for embedded lint checks) is now 7.0.

### Fixed

- `DebugTree` now finds first non-library class name which prevents exceptions in optimized builds where expected stackframes may have been inlined.
- Enforce 23-character truncated tag length until API 26 per AOSP sources.
- Support `Long` type for date/time format arguments when validating format strings in lint checks.
- Do not report string literal concatenation in lint checks on log message.


## [4.7.1] - 2018-06-28

 * Fix: Redundant argument lint check now works correctly on Kotlin sources.


## [4.7.0] - 2018-03-27

 * Fix: Support lint version 26.1.0.
 * Fix: Check single-argument log method in TimberExceptionLogging.


## [4.6.1] - 2018-02-12

 * Fix: Lint checks now handle more edge cases around exception and message source.
 * Fix: Useless `BuildConfig` class is no longer included.


## [4.6.0] - 2017-10-30

 * New: Lint checks have been ported to UAST, their stability improved, and quick-fix suggestions added. They require Android Gradle Plugin 3.0 or newer to run.
 * New: Added nullability annotations for Kotlin users.
 * Fix: Tag length truncation no longer occurs on API 24 or newer as the system no longer has a length restriction.
 * Fix: Handle when a `null` array is supplied for the message arguments. This can occur when using various bytecode optimization tools.


## [4.5.1] - 2017-01-20

 * Fix: String formatting lint check now correctly works with dates.


## [4.5.0] - 2017-01-09

 * New: Automatically truncate class name tags to Android's limit of 23 characters.
 * New: Lint check for detecting null/empty messages or using the exception message when logging an
   exception. Use the single-argument logging overloads instead.
 * Fix: Correct NPE in lint check when using String.format.


## [4.4.0] - 2016-12-06

 * New: `Tree.formatMessage` method allows customization of message formatting and rendering.
 * New: Lint checks ported to new IntelliJ PSI infrastructure.


## [4.3.1] - 2016-09-19

 * New: Add `isLoggable` convenience method which also provides the tag.


## [4.3.0] - 2016-08-18

 * New: Overloads for all log methods which accept only a `Throwable` without a message.


## [4.2.0] - 2016-08-12

 * New: `Timber.plant` now has a varargs overload for planting multiple trees at once.
 * New: minSdkVersion is now 9 because reasons.
 * Fix: Consume explicitly specified tag even if the message is determined as not loggable (due to level).
 * Fix: Allow lint checks to run when `Timber.tag(..).v(..)`-style logging is used.


## [4.1.2] - 2016-03-30

 * Fix: Tag-length lint check now only triggers on calls to `Timber`'s `tag` method. Previously it would
   match _any_ `tag` method and flag arguments longer than 23 characters.


## [4.1.1] - 2016-02-19

 * New: Add method for retreiving the number of installed trees.


## [4.1.0] - 2015-10-19

 * New: Consumer ProGuard rule automatically suppresses a warning for the use `@NonNls` on the 'message'
   argument for logging method. The warning was only for users running ProGuard and can safely be ignored.
 * New: Static `log` methods which accept a priority as a first argument makes dynamic logging at different
   levels easier to support.
 * Fix: Replace internal use of `Log.getStackTraceString` with our own implementation. This ensures that
   `UnknownHostException` errors are logged, which previously were suppressed.
 * Fix: 'BinaryOperationInTimber' lint rule now only triggers for string concatenation.


## [4.0.1] - 2015-10-07

 * Fix: TimberArgTypes lint rule now allows booleans and numbers in '%s' format markers.
 * Fix: Lint rules now support running on Java 7 VMs.


## [4.0.0] - 2015-10-07

 * New: Library is now an .aar! This means the lint rules are automatically applied to consuming
   projects.
 * New: `Tree.forest()` returns an immutable copy of all planted trees.
 * Fix: Ensure thread safety when logging and adding or removing trees concurrently.


## [3.1.0] - 2015-05-11

 * New: `Tree.isLoggable` method allows a tree to determine whether a statement should be logged
   based on its priority. Defaults to logging all levels.


## [3.0.2] - 2015-05-01

 * Fix: Strip multiple anonymous class markers (e.g., `$1$2`) from class names when `DebugTree`
   is creating an inferred tag.


## [3.0.1] - 2015-04-17

 * Fix: String formatting is now always applied when arguments are present. Previously it would
   only trigger when an exception was included.


## [3.0.0] - 2015-04-16

 * New: `Tree` and `DebugTree` APIs are much more extensible requiring only a single method to
   override.
 * New: `DebugTree` exposes `createStackElementTag` method for overriding to customize the
   reflection-based tag creation (for example, such as to add a line number).
 * WTF: Support for `wtf` log level.
 * `HollowTree` has been removed as it is no longer needed. Just extend `Tree`.
 * `TaggedTree` has been removed and its functionality folded into `Tree`. All `Tree` instances
   will receive any tags specified by a call to `tag`.
 * Fix: Multiple planted `DebugTree`s now each correctly received tags set from a call to `tag`.


## [2.7.1] - 2015-02-17

 * Fix: Switch method of getting calling class to be consistent across API levels.


## [2.7.0] - 2015-02-17

 * New: `DebugTree` subclasses can now override `logMessage` for access to the priority, tag, and
   entire message for every log.
 * Fix: Prevent overriding `Tree` and `TaggedTree` methods on `DebugTree`.


## [2.6.0] - 2015-02-17

 * New: `DebugTree` subclasses can now override `createTag()` to specify log tags. `nextTag()` is
   also accessible for querying if an explicit tag was set.


## [2.5.1] - 2015-01-19

 * Fix: Properly split lines which contain both newlines and are over 4000 characters.
 * Explicitly forbid `null` tree instances.


## [2.5.0] - 2014-11-08

 * New: `Timber.asTree()` exposes functionality as a `Tree` instance rather than static methods.


## [2.4.2] - 2014-11-07

 * Eliminate heap allocation when dispatching log calls.


## [2.4.1] - 2014-06-19

 * Fix: Calls with no message but a `Throwable` are now correctly logged.


## [2.4.0] - 2014-06-10

 * New: `uproot` and `uprootAll` methods allow removing trees.


## [2.3.0] - 2014-05-21

 * New: Messages longer than 4000 characters will be split into multiple lines.


## [2.2.2] - 2014-02-12

 * Fix: Include debug level in previous fix which avoids formatting messages with no arguments.


## [2.2.1] - 2014-02-11

 * Fix: Do not attempt to format log messages which do not have arguments.


## [2.2.0] - 2014-02-02

 * New: verbose log level added (`v()`).
 * New: `timber-lint` module adds lint check to ensure you are calling `Timber` and not `Log`.
 * Fix: Specifying custom tags is now thread-safe.


## [2.1.0] - 2013-11-21

 * New: `tag` method allows specifying custom one-time tag. Redux!


## [2.0.0] - 2013-10-21

 * Logging API is now exposed as static methods on `Timber`. Behavior is added by installing `Tree`
   instances for logging.


## [1.1.0] - 2013-07-22

 * New: `tag` method allows specifying custom one-time tag.
 * Fix: Exception-containing methods now log at the correct level.


## [1.0.0] - 2013-07-17

Initial cut. (Get it?)




[Unreleased]: https://github.com/JakeWharton/timber/compare/5.0.1...HEAD
[5.0.1]: https://github.com/JakeWharton/timber/releases/tag/5.0.1
[5.0.0]: https://github.com/JakeWharton/timber/releases/tag/5.0.0
[4.7.1]: https://github.com/JakeWharton/timber/releases/tag/4.7.1
[4.7.0]: https://github.com/JakeWharton/timber/releases/tag/4.7.0
[4.6.1]: https://github.com/JakeWharton/timber/releases/tag/4.6.1
[4.6.0]: https://github.com/JakeWharton/timber/releases/tag/4.6.0
[4.5.1]: https://github.com/JakeWharton/timber/releases/tag/4.5.1
[4.5.0]: https://github.com/JakeWharton/timber/releases/tag/4.5.0
[4.4.0]: https://github.com/JakeWharton/timber/releases/tag/4.4.0
[4.3.1]: https://github.com/JakeWharton/timber/releases/tag/4.3.1
[4.3.0]: https://github.com/JakeWharton/timber/releases/tag/4.3.0
[4.2.0]: https://github.com/JakeWharton/timber/releases/tag/4.2.0
[4.1.2]: https://github.com/JakeWharton/timber/releases/tag/4.1.2
[4.1.1]: https://github.com/JakeWharton/timber/releases/tag/4.1.1
[4.1.0]: https://github.com/JakeWharton/timber/releases/tag/4.1.0
[4.0.1]: https://github.com/JakeWharton/timber/releases/tag/4.0.1
[4.0.0]: https://github.com/JakeWharton/timber/releases/tag/4.0.0
[3.1.0]: https://github.com/JakeWharton/timber/releases/tag/3.1.0
[3.0.2]: https://github.com/JakeWharton/timber/releases/tag/3.0.2
[3.0.1]: https://github.com/JakeWharton/timber/releases/tag/3.0.1
[3.0.0]: https://github.com/JakeWharton/timber/releases/tag/3.0.0
[2.7.1]: https://github.com/JakeWharton/timber/releases/tag/2.7.1
[2.7.0]: https://github.com/JakeWharton/timber/releases/tag/2.7.0
[2.6.0]: https://github.com/JakeWharton/timber/releases/tag/2.6.0
[2.5.1]: https://github.com/JakeWharton/timber/releases/tag/2.5.1
[2.5.0]: https://github.com/JakeWharton/timber/releases/tag/2.5.0
[2.4.2]: https://github.com/JakeWharton/timber/releases/tag/2.4.2
[2.4.1]: https://github.com/JakeWharton/timber/releases/tag/2.4.1
[2.4.0]: https://github.com/JakeWharton/timber/releases/tag/2.4.0
[2.3.0]: https://github.com/JakeWharton/timber/releases/tag/2.3.0
[2.2.2]: https://github.com/JakeWharton/timber/releases/tag/2.2.2
[2.2.1]: https://github.com/JakeWharton/timber/releases/tag/2.2.1
[2.2.0]: https://github.com/JakeWharton/timber/releases/tag/2.2.0
[2.1.0]: https://github.com/JakeWharton/timber/releases/tag/2.1.0
[2.0.0]: https://github.com/JakeWharton/timber/releases/tag/2.0.0
[1.1.0]: https://github.com/JakeWharton/timber/releases/tag/1.1.0
[1.0.0]: https://github.com/JakeWharton/timber/releases/tag/1.0.0
