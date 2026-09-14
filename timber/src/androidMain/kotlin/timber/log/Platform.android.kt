package timber.log

import android.util.Log

internal actual fun writeLog(priority: Int, tag: String?, message: String) {
  when (priority) {
    Priority.ASSERT -> Log.wtf(tag, message)
    else -> Log.println(priority, tag, message)
  }
}
