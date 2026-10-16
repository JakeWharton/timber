package timber.log

import java.io.PrintWriter
import java.io.StringWriter
import timber.log.Timber.Forest
import timber.log.Timber.Tree

internal actual fun getStackTraceString(t: Throwable): String {
  // Don't replace this with Log.getStackTraceString() - it hides
  // UnknownHostException, which is not what we want.
  val sw = StringWriter(256)
  val pw = PrintWriter(sw, false)
  t.printStackTrace(pw)
  pw.flush()
  return sw.toString()
}

internal actual fun String.format(args: Array<out Any?>) = this.format(*args)

internal actual fun callerStackElement(): StackTraceElement? {
  val st = Throwable().stackTrace
  val thisClass = st.firstOrNull()?.className()
  return st.firstOrNull { it.className() != thisClass && it.className !in fqcnIgnore }
}

private val fqcnIgnore =
  listOf(
    Timber::class.java.name,
    Forest::class.java.name,
    Tree::class.java.name,
    Timber.DebugTree::class.java.name,
  )
