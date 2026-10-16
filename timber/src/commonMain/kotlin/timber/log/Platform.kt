package timber.log

internal expect fun writeLog(priority: Int, tag: String?, message: String)

internal expect fun getStackTraceString(t: Throwable): String

internal expect fun String.format(args: Array<out Any?>): String

internal expect fun maxLogLength(): Int

internal expect fun maxTagLength(): Int

internal expect fun callerStackElement(): StackTraceElement?
