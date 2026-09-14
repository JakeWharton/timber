package timber.log

import android.os.Build
import android.util.Log
import java.util.regex.Pattern
import kotlin.math.min
import timber.log.Timber.Forest
import timber.log.Timber.Tree

actual open class DebugTree actual constructor() : Tree() {
  private val fqcnIgnore =
    listOf(
      Timber::class.java.name,
      Forest::class.java.name,
      Tree::class.java.name,
      DebugTree::class.java.name,
    )

  override val tag: String?
    get() =
      super.tag
        ?: Throwable()
          .stackTrace
          .first { it.className !in fqcnIgnore }
          .let(::createStackElementTag)

  /**
   * Extract the tag which should be used for the message from the `element`. By default this will
   * use the class name without any anonymous class suffixes (e.g., `Foo$1` becomes `Foo`).
   *
   * Note: This will not be called if a [manual tag][.tag] was specified.
   */
  protected open fun createStackElementTag(element: StackTraceElement): String? {
    var tag = element.className.substringAfterLast('.')
    val m = ANONYMOUS_CLASS.matcher(tag)
    if (m.find()) {
      tag = m.replaceAll("")
    }
    // Tag length limit was removed in API 26.
    return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
      tag
    } else {
      tag.substring(0, MAX_TAG_LENGTH)
    }
  }

  /**
   * Break up `message` into maximum-length chunks (if needed) and send to either
   * [Log.println()][Log.println] or [Log.wtf()][Log.wtf] for logging.
   *
   * {@inheritDoc}
   */
  override fun log(priority: Priority, tag: String?, message: String, t: Throwable?) {
    if (message.length < MAX_LOG_LENGTH) {
      writeLog(priority, tag, message)
      return
    }

    // Split by line, then ensure each line can fit into Log's maximum length.
    var i = 0
    val length = message.length
    while (i < length) {
      var newline = message.indexOf('\n', i)
      newline = if (newline != -1) newline else length
      do {
        val end = min(newline, i + MAX_LOG_LENGTH)
        val part = message.substring(i, end)
        writeLog(priority, tag, part)
        i = end
      } while (i < newline)
      i++
    }
  }

  companion object {
    private const val MAX_LOG_LENGTH = 4000
    private const val MAX_TAG_LENGTH = 23
    private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
  }

}
