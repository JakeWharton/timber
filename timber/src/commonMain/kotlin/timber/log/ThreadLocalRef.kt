package timber.log

/**
 * A per thread value holder. The equivalent of `java.lang.ThreadLocal` on JVM compatible platforms.
 */
internal expect class ThreadLocalRef<T>() {
  fun get(): T?

  fun set(value: T?)

  fun remove()
}
