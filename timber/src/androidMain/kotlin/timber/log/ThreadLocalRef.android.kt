package timber.log

internal actual class ThreadLocalRef<T> {
  private val delegate = ThreadLocal<T>()
  actual fun get(): T? = delegate.get()

  actual fun set(value: T?) {
    delegate.set(value)
  }

  actual fun remove() {
    delegate.remove()
  }
}
