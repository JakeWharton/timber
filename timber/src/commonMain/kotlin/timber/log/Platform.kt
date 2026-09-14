@file:JvmName("Platform")
package timber.log

import kotlin.jvm.JvmName

internal expect fun writeLog(priority: Int, tag: String?, message: String)

internal expect fun getStackTraceString(t: Throwable): String

internal expect fun String.format(args: Array<out Any?>): String
