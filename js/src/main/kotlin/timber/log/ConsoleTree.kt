package timber.log

import kotlin.js.Console

class ConsoleTree(private val console: Console = kotlin.js.console) : Tree() {
  override fun isLoggable(priority: Int, tag: String?) = priority != Timber.VERBOSE

  override fun performLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
    when (priority) {
      Timber.ERROR, Timber.ASSERT -> console.error(message)
      Timber.WARNING -> console.warn(message)
      Timber.INFO -> console.info(message)
      Timber.DEBUG -> console.log(message)
      Timber.VERBOSE -> {} // TODO use console.debug here?
      else -> error("Unknown priority level: $priority")
    }
  }
}
