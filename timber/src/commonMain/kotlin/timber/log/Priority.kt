package timber.log

/**
 * Log levels.
 * Note: this is a direct mapping to android.util.Log values
 */
object Priority {
  const val VERBOSE = 2
  const val DEBUG = 3
  const val INFO = 4
  const val WARN = 5
  const val ERROR = 6
  const val ASSERT = 7

  /**
   * @return the string representation of this [priority]
   */
  fun name(priority: Int): String {
    return when (priority) {
      VERBOSE -> "Verbose"
      DEBUG -> "Debug"
      INFO -> "Info"
      WARN -> "Warn"
      ERROR -> "Error"
      ASSERT -> "Assert"
      else -> throw IllegalArgumentException("unsupported priority: $priority")
    }
  }
}
