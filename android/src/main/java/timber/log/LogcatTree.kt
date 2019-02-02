package timber.log

import android.os.Build
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

private const val MAX_LOG_LENGTH = 4000
private const val MAX_TAG_LENGTH = 23

/**
 * A [Tree] which forwards to [Log].
 *
 * Calls to [performLog] are sent to either [Log.wtf] (for [Timber.ASSERT] level) or [Log.println]
 * (for everything else). Prior to API 24, log tags are automatically truncated to 23 characters.
 * Log messages will be split if they exceed 4000 characters to work around the platform message
 * limit.
 *
 * Note: This does not check [Log.isLoggable] by default. Call [withCompliantLogging] for an
 * instance which delegates to this method prior to logging.
 */
class LogcatTree constructor(private val tagResolver: () -> String) : Tree() {

  @JvmOverloads
  constructor(defaultTag: String = "App") : this({ defaultTag })

  /** Return a new [Tree] that checks [Log.isLoggable] prior to logging. */
  fun withCompliantLogging() = object : Tree() {
    override fun isLoggable(priority: Int, tag: String?): Boolean {
      return Log.isLoggable(tag.asSafeTag(), priority)
    }

    override fun performLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
      this@LogcatTree.performLog(priority, tag, throwable, message)
    }
  }

  override fun performLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
    val safeTag = tag.asSafeTag()

    val fullMessage = if (message != null) {
      if (throwable != null) {
        "$message\n${throwable.stackTraceString}"
      } else {
        message
      }
    } else if (throwable != null) {
      throwable.stackTraceString
    } else {
      return // Nothing to do!
    }

    val length = fullMessage.length
    if (length <= MAX_LOG_LENGTH) {
      // Fast path for small messages which can fit in a single call.
      if (priority == Timber.ASSERT) {
        Log.wtf(safeTag, fullMessage)
      } else {
        Log.println(priority, safeTag, fullMessage)
      }
      return
    }

    // Slow path: Split by line, then ensure each line can fit into Log's maximum length.
    // TODO use lastIndexOf instead of indexOf to batch multiple lines into single calls.
    var i = 0
    while (i < length) {
      var newline = fullMessage.indexOf('\n', i)
      newline = if (newline != -1) newline else length
      do {
        val end = Math.min(newline, i + MAX_LOG_LENGTH)
        val part = fullMessage.substring(i, end)
        if (priority == Log.ASSERT) {
          Log.wtf(safeTag, part)
        } else {
          Log.println(priority, safeTag, part)
        }
        i = end
      } while (i < newline)
      i++
    }
  }

  private fun String?.asSafeTag(): String {
    val tag = this ?: tagResolver.invoke()
    // Tag length limit was removed in API 24.
    if (Build.VERSION.SDK_INT < 24 && tag.length > MAX_TAG_LENGTH) {
      return tag.substring(0, MAX_TAG_LENGTH)
    }
    return tag
  }

  private val Throwable.stackTraceString get(): String {
    // DO NOT replace this with Log.getStackTraceString() - it hides UnknownHostException, which is
    // not what we want.
    val sw = StringWriter(256)
    val pw = PrintWriter(sw, false)
    printStackTrace(pw)
    pw.flush()
    return sw.toString()
  }
}
