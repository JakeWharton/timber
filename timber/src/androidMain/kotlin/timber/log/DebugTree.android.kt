package timber.log

import android.os.Build

actual open class DebugTree actual constructor() : JvmAndroidDebugTree() {

  override fun maxLogLength(): Int = MAX_LOG_LENGTH

  override fun maxTagLength(): Int {
    // Tag length limit was removed in API 26.
    return if (Build.VERSION.SDK_INT >= 26) {
      Int.MAX_VALUE
    } else {
      MAX_TAG_LENGTH
    }
  }

  companion object {
    private const val MAX_LOG_LENGTH = 4000
    private const val MAX_TAG_LENGTH = 23
  }
}
