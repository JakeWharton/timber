package timber.log

internal actual fun writeLog(priority: Int, tag: String?, message: String) {
  val line = buildString {
    append(Priority.name(priority))
    if (!tag.isNullOrBlank()) append("/").append(tag)
    append(": ").append(message)
  }
  when {
    priority >= Priority.WARN -> System.err.println(line)
    else -> println(line)
  }
}

internal actual fun maxLogLength() = Int.MAX_VALUE

internal actual fun maxTagLength() = Int.MAX_VALUE
