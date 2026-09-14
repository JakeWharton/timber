package timber.log

internal actual fun writeLog(priority: Int, tag: String?, message: String) {
  val sb = StringBuilder().apply {
    append(Priority.name(priority))
    if (!tag.isNullOrBlank()) {
      append("/")
      append(tag)
    }
    append(": ")
    append(message)
  }
  println(sb.toString())
}
