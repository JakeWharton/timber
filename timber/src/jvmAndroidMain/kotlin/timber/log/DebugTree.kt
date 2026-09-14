package timber.log

import java.util.regex.Pattern
import kotlin.math.min
import timber.log.Timber.Forest
import timber.log.Timber.Tree

open class JvmAndroidDebugTree : Tree() {
  private val fqcnIgnore = listOf(
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
    return when {
        maxTagLength() < Int.MAX_VALUE && tag.length > maxTagLength() -> tag.substring(0, maxTagLength())
        else -> tag
    }
  }

  /**
   * Break up `message` into maximum-length chunks (if needed) and send to either for logging.
   *
   * {@inheritDoc}
   */
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    if (message.length < maxLogLength()) {
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
        val end = min(newline, i + maxLogLength())
        val part = message.substring(i, end)
        writeLog(priority, tag, part)
        i = end
      } while (i < newline)
      i++
    }
  }

  open fun maxLogLength()  = Int.MAX_VALUE

  open fun maxTagLength() = Int.MAX_VALUE

  companion object {
    private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
  }
}
