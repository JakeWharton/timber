package timber.log

internal actual typealias ThreadLocal<T> = java.lang.ThreadLocal<T>

internal actual fun <T> ThreadLocal<T>.get(): T {
  return this.get()
}

internal actual fun <T> ThreadLocal<T>.set(value: T?) {
  this.set(value)
}

internal actual fun <T> ThreadLocal<T>.remove() {
  this.remove()
}
