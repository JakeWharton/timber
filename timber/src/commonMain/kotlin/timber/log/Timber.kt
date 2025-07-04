package timber.log

/** Logging for lazy people. */
expect class Timber private constructor() {

  /** A facade for handling logging calls. Install instances via [`Timber.plant()`][.plant]. */
  abstract class Tree {

    /** Log a verbose message with optional format args. */
    open fun v(message: String?, vararg args: Any?)

    /** Log a verbose exception and a message with optional format args. */
    open fun v(t: Throwable?, message: String?, vararg args: Any?)

    /** Log a verbose exception. */
    open fun v(t: Throwable?)

    /** Log a debug message with optional format args. */
    open fun d(message: String?, vararg args: Any?)

    /** Log a debug exception and a message with optional format args. */
    open fun d(t: Throwable?, message: String?, vararg args: Any?)

    /** Log a debug exception. */
    open fun d(t: Throwable?)

    /** Log an info message with optional format args. */
    open fun i(message: String?, vararg args: Any?)

    /** Log an info exception and a message with optional format args. */
    open fun i(t: Throwable?, message: String?, vararg args: Any?)

    /** Log an info exception. */
    open fun i(t: Throwable?)

    /** Log a warning message with optional format args. */
    open fun w(message: String?, vararg args: Any?)

    /** Log a warning exception and a message with optional format args. */
    open fun w(t: Throwable?, message: String?, vararg args: Any?)

    /** Log a warning exception. */
    open fun w(t: Throwable?)

    /** Log an error message with optional format args. */
    open fun e(message: String?, vararg args: Any?)

    /** Log an error exception and a message with optional format args. */
    open fun e(t: Throwable?, message: String?, vararg args: Any?)

    /** Log an error exception. */
    open fun e(t: Throwable?)

    /** Log an assert message with optional format args. */
    open fun wtf(message: String?, vararg args: Any?)

    /** Log an assert exception and a message with optional format args. */
    open fun wtf(t: Throwable?, message: String?, vararg args: Any?)

    /** Log an assert exception. */
    open fun wtf(t: Throwable?)

    /** Log at `priority` a message with optional format args. */
    open fun log(priority: Int, message: String?, vararg args: Any?)

    /** Log at `priority` an exception and a message with optional format args. */
    open fun log(priority: Int, t: Throwable?, message: String?, vararg args: Any?)

    /** Log at `priority` an exception. */
    open fun log(priority: Int, t: Throwable?)

    /** Return whether a message at `priority` or `tag` should be logged. */
    protected open fun isLoggable(tag: String?, priority: Int): Boolean

    /** Formats a log message with optional arguments. */
    protected open fun formatMessage(message: String, args: Array<out Any?>): String

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.
     *
     * @param priority Log level. See [Log] for constants.
     * @param tag Explicit or inferred tag. May be `null`.
     * @param message Formatted log message.
     * @param t Accompanying exceptions. May be `null`.
     */
    protected abstract fun log(priority: Int, tag: String?, message: String, t: Throwable?)
  }

  companion object Forest : Tree {

    /** Set a one-time tag for use on the next logging call. */
    fun tag(tag: String): Tree

    /** Add a new logging tree. */
    fun plant(tree: Tree)

    /** Adds new logging trees. */
    fun plant(vararg trees: Tree)

    /** Remove a planted tree. */
    fun uproot(tree: Tree)

    /** Remove all planted trees. */
    fun uprootAll()

    /** Return a copy of all planted [trees][Tree]. */
    fun forest(): List<Tree>

    val treeCount: Int
  }
}
