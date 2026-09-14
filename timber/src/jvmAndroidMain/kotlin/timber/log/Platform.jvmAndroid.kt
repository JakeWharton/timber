package timber.log

import java.io.PrintWriter
import java.io.StringWriter

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
