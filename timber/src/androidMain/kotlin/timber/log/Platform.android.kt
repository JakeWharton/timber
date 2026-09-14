package timber.log

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

internal actual fun writeLog(priority: Priority, tag: String?, message: String) {
  when (priority) {
    Priority.ASSERT -> Log.wtf(tag, message)
    else -> Log.println(priority.toInt(), tag, message)
  }
}

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


// TODO: Write test for this
private fun Priority.toInt(): Int = ordinal + 2 // Logs starts from 2
