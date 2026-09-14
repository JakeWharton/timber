package timber.log

import android.os.Build
import android.util.Log

internal actual fun writeLog(priority: Int, tag: String?, message: String) {
  when (priority) {
    Priority.ASSERT -> Log.wtf(tag, message)
    else -> Log.println(priority, tag, message)
  }
}

internal actual fun maxLogLength(): Int = MAX_LOG_LENGTH

internal actual fun maxTagLength(): Int {
  // Tag length limit was removed in API 26.
  return if (Build.VERSION.SDK_INT >= 26) {
    Int.MAX_VALUE
  } else {
    MAX_TAG_LENGTH
  }
}

private const val MAX_LOG_LENGTH = 4000
private const val MAX_TAG_LENGTH = 23
