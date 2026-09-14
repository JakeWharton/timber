package timber.log
// TODO: Doc
internal expect class ThreadLocalRef<T>() {
  fun get(): T?
  fun set(value: T?)
  fun remove()
}
