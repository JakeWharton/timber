package timber.log

object Timber {
  private val forestList = mutableListOf<Tree>()
  private var forestArray: Array<Tree> = emptyArray()

  val trees get() = forestArray.toList()

  val size get() = forestArray.size

  fun uprootAll() {
    synchronized(forestList) {
      forestList.clear()
      forestArray = emptyArray()
    }
  }

  fun uproot(tree: Tree) {
    synchronized(forestList) {
      require(forestList.remove(tree)) { "Cannot uproot tree which is not planted: $tree" }
      forestArray = forestList.toTypedArray()
    }
  }

  fun plant(tree: Tree) {
    synchronized(forestList) {
      forestList.add(tree)
      forestArray = forestList.toTypedArray()
    }
  }

  fun plant(vararg trees: Tree) {
    synchronized(forestList) {
      forestList.addAll(trees)
      forestArray = forestList.toTypedArray()
    }
  }

  fun plantAll(trees: Iterable<Tree>) {
    synchronized(forestList) {
      forestList.addAll(trees)
      forestArray = forestList.toTypedArray()
    }
  }

  fun isLoggable(priority: Int, tag: String? = null) = forestArray.any { it.isLoggable(priority, tag) }

  fun log(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
    forestArray.forEach { it.log(priority, tag, throwable, message) }
  }

  /** Invoked only when [isLoggable] has returned true. */
  @PublishedApi
  internal fun rawLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
    forestArray.forEach { it.rawLog(priority, tag, throwable, message) }
  }

  fun tagged(tag: String): Tree {
    val taggedTag = tag
    return object : Tree() {
      override fun isLoggable(priority: Int, tag: String?): Boolean {
        return Timber.isLoggable(priority, tag ?: taggedTag)
      }

      override fun performLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
        Timber.log(priority, tag ?: taggedTag, throwable, message)
      }
    }
  }

  const val VERBOSE = 2
  const val DEBUG = 3
  const val INFO = 4
  const val WARNING = 5
  const val ERROR = 6
  const val ASSERT = 7
}

inline fun Timber.log(priority: Int, throwable: Throwable? = null, message: () -> String) {
  if (isLoggable(priority, null)) {
    rawLog(priority, null, throwable, message())
  }
}

inline fun Timber.assert(throwable: Throwable? = null, message: () -> String) {
  log(ASSERT, throwable, message)
}

inline fun Timber.error(throwable: Throwable? = null, message: () -> String) {
  log(ERROR, throwable, message)
}

inline fun Timber.warn(throwable: Throwable? = null, message: () -> String) {
  log(WARNING, throwable, message)
}

inline fun Timber.info(throwable: Throwable? = null, message: () -> String) {
  log(INFO, throwable, message)
}

inline fun Timber.debug(throwable: Throwable? = null, message: () -> String) {
  log(DEBUG, throwable, message)
}

inline fun Timber.verbose(throwable: Throwable? = null, message: () -> String) {
  log(VERBOSE, throwable, message)
}
