package timber.log

actual typealias StackTraceElement = java.lang.StackTraceElement

internal actual fun StackTraceElement.className(): String = this.className
