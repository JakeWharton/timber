package timber.log

/**
 * A per thread value holder. The equivalent of `java.lang.ThreadLocal` on JVM compatible platforms.
 */
internal expect class ThreadLocal<T>

internal expect fun <T> ThreadLocal<T>.get(): T
internal expect fun <T> ThreadLocal<T>.set(value: T?)
internal expect fun<T> ThreadLocal<T>.remove()
