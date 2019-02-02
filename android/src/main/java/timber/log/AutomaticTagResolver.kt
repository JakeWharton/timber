package timber.log

import java.util.regex.Pattern

/**
 * Can be passed to [LogcatTree]'s constructor in order to treat the class at [callStackIndex] in
 * the stacktrace as the default tag for the current log. Not recommended for use in release
 * builds.
 *
 * @param callStackIndex How far down the stacktrace to look for the log. Should be 6 if making
 * normal Timber log calls, could change if wrapping Timber
 * @param trimFunctionName Trims the function name if present before returning the tag
 */
class AutomaticTagResolver(
    private val callStackIndex: Int = 6,
    private val trimFunctionName: Boolean = true
) : () -> String {

  override fun invoke(): String {
    // DO NOT switch this to Thread.getCurrentThread().getStackTrace(). The test may pass
    // because Robolectric runs them on the JVM but on Android the elements are different.
    val stackTrace = Throwable().stackTrace

    if (stackTrace.size <= callStackIndex)
      throw IllegalStateException("Synthetic stacktrace didn't have enough elements: are you using proguard?")

    return createStackElementTag(stackTrace[callStackIndex])
  }

  /**
   * Extract the tag which should be used for the message from the `element`. By default this will use the class name
   * without any anonymous class suffixes (e.g., `Foo$1` becomes `Foo`).
   */
  private fun createStackElementTag(element: StackTraceElement): String {
    var tag = element.className

    // Remove anonymous class signifiers (e.g. "$1") from the tag:
    val matcher = ANONYMOUS_CLASS_PATTERN.matcher(tag)
    if (matcher.find())
      tag = matcher.replaceAll("")

    // Remove leading package name (e.g. "com.jakewharton.timber.log.") and potential trailing function name (e.g.
    // "$yourFunction") from the tag:
    val tagEnd =
        if (trimFunctionName && tag.contains(FUNCTION_SIGNIFIER)) tag.indexOf(FUNCTION_SIGNIFIER)
        else tag.length
    return tag.substring(tag.lastIndexOf('.') + 1, tagEnd)
  }

  private companion object {
    const val FUNCTION_SIGNIFIER = '$'
    private val ANONYMOUS_CLASS_PATTERN = Pattern.compile("(\\$\\d+)+$")
  }
}
