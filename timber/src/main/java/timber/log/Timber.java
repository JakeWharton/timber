package timber.log;

import android.util.Log;
import android.util.SparseBooleanArray;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Logging for lazy people. */
public final class Timber {
  /** Log a verbose message with optional format args. */
  public static void v(String message, Object... args) {
    TREE_OF_SOULS.v(message, args);
  }

  /** Log a verbose exception and a message with optional format args. */
  public static void v(Throwable t, String message, Object... args) {
    TREE_OF_SOULS.v(t, message, args);
  }

  /** Log a debug message with optional format args. */
  public static void d(String message, Object... args) {
    TREE_OF_SOULS.d(message, args);
  }

  /** Log a debug exception and a message with optional format args. */
  public static void d(Throwable t, String message, Object... args) {
    TREE_OF_SOULS.d(t, message, args);
  }

  /** Log an info message with optional format args. */
  public static void i(String message, Object... args) {
    TREE_OF_SOULS.i(message, args);
  }

  /** Log an info exception and a message with optional format args. */
  public static void i(Throwable t, String message, Object... args) {
    TREE_OF_SOULS.i(t, message, args);
  }

  /** Log a warning message with optional format args. */
  public static void w(String message, Object... args) {
    TREE_OF_SOULS.w(message, args);
  }

  /** Log a warning exception and a message with optional format args. */
  public static void w(Throwable t, String message, Object... args) {
    TREE_OF_SOULS.w(t, message, args);
  }

  /** Log an error message with optional format args. */
  public static void e(String message, Object... args) {
    TREE_OF_SOULS.e(message, args);
  }

  /** Log an error exception and a message with optional format args. */
  public static void e(Throwable t, String message, Object... args) {
    TREE_OF_SOULS.e(t, message, args);
  }

  /**
   * A view into Timber's planted trees as a tree itself. This can be used for injecting a logger
   * instance rather than using static methods or to facilitate testing.
   */
  public static Tree asTree() {
    return TREE_OF_SOULS;
  }

  /** Set a one-time tag for use on the next logging call. */
  public static Tree tag(String tag) {
    for (int index = 0, size = TAGGED_TREES.size(); index < size; index++) {
      ((TaggedTree) FOREST.get(TAGGED_TREES.keyAt(index))).tag(tag);
    }
    return TREE_OF_SOULS;
  }

  /** Add a new logging tree. */
  public static void plant(Tree tree) {
    if (tree == null) {
      throw new NullPointerException("tree == null");
    }
    if (tree == TREE_OF_SOULS) {
      throw new IllegalArgumentException("Cannot plant Timber into itself.");
    }
    if (tree instanceof TaggedTree) {
      TAGGED_TREES.append(FOREST.size(), true);
    }
    FOREST.add(tree);
  }

  /** Remove a planted tree. */
  public static void uproot(Tree tree) {
    for (int i = 0, size = FOREST.size(); i < size; i++) {
      if (FOREST.get(i) == tree) {
        TAGGED_TREES.delete(i);
        FOREST.remove(i);
        return;
      }
    }
    throw new IllegalArgumentException("Cannot uproot tree which is not planted: " + tree);
  }

  /** Remove all planted trees. */
  public static void uprootAll() {
    TAGGED_TREES.clear();
    FOREST.clear();
  }

  static final List<Tree> FOREST = new CopyOnWriteArrayList<Tree>();
  static final SparseBooleanArray TAGGED_TREES = new SparseBooleanArray();

  /** A {@link Tree} that delegates to all planted trees in the {@link #FOREST forest}. */
  private static final Tree TREE_OF_SOULS = new Tree() {
    @Override public void v(String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).v(message, args);
      }
    }

    @Override public void v(Throwable t, String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).v(t, message, args);
      }
    }

    @Override public void d(String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).d(message, args);
      }
    }

    @Override public void d(Throwable t, String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).d(t, message, args);
      }
    }

    @Override public void i(String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).i(message, args);
      }
    }

    @Override public void i(Throwable t, String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).i(t, message, args);
      }
    }

    @Override public void w(String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).w(message, args);
      }
    }

    @Override public void w(Throwable t, String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).w(t, message, args);
      }
    }

    @Override public void e(String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).e(message, args);
      }
    }

    @Override public void e(Throwable t, String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).e(t, message, args);
      }
    }
  };

  private Timber() {
  }

  /** A facade for handling logging calls. Install instances via {@link #plant}. */
  public interface Tree {
    /** Log a verbose message with optional format args. */
    void v(String message, Object... args);

    /** Log a verbose exception and a message with optional format args. */
    void v(Throwable t, String message, Object... args);

    /** Log a debug message with optional format args. */
    void d(String message, Object... args);

    /** Log a debug exception and a message with optional format args. */
    void d(Throwable t, String message, Object... args);

    /** Log an info message with optional format args. */
    void i(String message, Object... args);

    /** Log an info exception and a message with optional format args. */
    void i(Throwable t, String message, Object... args);

    /** Log a warning message with optional format args. */
    void w(String message, Object... args);

    /** Log a warning exception and a message with optional format args. */
    void w(Throwable t, String message, Object... args);

    /** Log an error message with optional format args. */
    void e(String message, Object... args);

    /** Log an error exception and a message with optional format args. */
    void e(Throwable t, String message, Object... args);
  }

  /** A facade for attaching tags to logging calls. Install instances via {@link #plant} */
  public interface TaggedTree extends Tree {
    /** Set a one-time tag for use on the next logging call. */
    void tag(String tag);
  }

  /** A {@link Tree} for debug builds. Automatically infers the tag from the calling class. */
  public static class DebugTree implements TaggedTree {
    private static final int MAX_LOG_LENGTH = 4000;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<String>();

    /**
     * Returns an explicitly set tag for the next log message or {@code null}. Calling this method
     * clears any set tag so it may only be called once.
     */
    protected final String nextTag() {
      String tag = NEXT_TAG.get();
      if (tag != null) {
        NEXT_TAG.remove();
      }
      return tag;
    }

    /**
     * Creates a tag for a log message.
     * <p>
     * By default this method will check {@link #nextTag()} for an explicit tag. If there is no
     * explicit tag, the class name of the caller will be used by inspecting the stack trace of the
     * current thread.
     * <p>
     * Note: Do not call {@code super.createTag()} if you override this method. It will produce
     * incorrect results.
     */
    protected String createTag() {
      String tag = nextTag();
      if (tag != null) {
        return tag;
      }

      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      if (stackTrace.length < 7) {
        throw new IllegalStateException(
            "Synthetic stacktrace didn't have enough elements: are you using proguard?");
      }
      tag = stackTrace[6].getClassName();
      Matcher m = ANONYMOUS_CLASS.matcher(tag);
      if (m.find()) {
        tag = m.replaceAll("");
      }
      return tag.substring(tag.lastIndexOf('.') + 1);
    }

    private static String maybeFormat(String message, Object... args) {
      // If no varargs are supplied, treat it as a request to log the string without formatting.
      return args.length == 0 ? message : String.format(message, args);
    }

    @Override public final void v(String message, Object... args) {
      throwShade(Log.VERBOSE, maybeFormat(message, args), null);
    }

    @Override public final void v(Throwable t, String message, Object... args) {
      throwShade(Log.VERBOSE, maybeFormat(message, args), t);
    }

    @Override public final void d(String message, Object... args) {
      throwShade(Log.DEBUG, maybeFormat(message, args), null);
    }

    @Override public final void d(Throwable t, String message, Object... args) {
      throwShade(Log.DEBUG, maybeFormat(message, args), t);
    }

    @Override public final void i(String message, Object... args) {
      throwShade(Log.INFO, maybeFormat(message, args), null);
    }

    @Override public final void i(Throwable t, String message, Object... args) {
      throwShade(Log.INFO, maybeFormat(message, args), t);
    }

    @Override public final void w(String message, Object... args) {
      throwShade(Log.WARN, maybeFormat(message, args), null);
    }

    @Override public final void w(Throwable t, String message, Object... args) {
      throwShade(Log.WARN, maybeFormat(message, args), t);
    }

    @Override public final void e(String message, Object... args) {
      throwShade(Log.ERROR, maybeFormat(message, args), null);
    }

    @Override public final void e(Throwable t, String message, Object... args) {
      throwShade(Log.ERROR, maybeFormat(message, args), t);
    }

    private void throwShade(int priority, String message, Throwable t) {
      if (message == null || message.length() == 0) {
        if (t == null) {
          return; // Swallow message if it's null and there's no throwable.
        }
        message = Log.getStackTraceString(t);
      } else if (t != null) {
        message += "\n" + Log.getStackTraceString(t);
      }

      String tag = createTag();
      logMessage(priority, tag, message);
    }

    /** Log a message! */
    protected void logMessage(int priority, String tag, String message) {
      if (message.length() < MAX_LOG_LENGTH) {
        Log.println(priority, tag, message);
        return;
      }

      // Split by line, then ensure each line can fit into Log's maximum length.
      for (int i = 0, length = message.length(); i < length; i++) {
        int newline = message.indexOf('\n', i);
        newline = newline != -1 ? newline : length;
        do {
          int end = Math.min(newline, i + MAX_LOG_LENGTH);
          Log.println(priority, tag, message.substring(i, end));
          i = end;
        } while (i < newline);
      }
    }

    @Override public void tag(String tag) {
      NEXT_TAG.set(tag);
    }
  }

  /** A {@link Tree} which does nothing. Useful for extending. */
  public static class HollowTree implements Tree {
    @Override public void v(String message, Object... args) {
    }

    @Override public void v(Throwable t, String message, Object... args) {
    }

    @Override public void d(String message, Object... args) {
    }

    @Override public void d(Throwable t, String message, Object... args) {
    }

    @Override public void i(String message, Object... args) {
    }

    @Override public void i(Throwable t, String message, Object... args) {
    }

    @Override public void w(String message, Object... args) {
    }

    @Override public void w(Throwable t, String message, Object... args) {
    }

    @Override public void e(String message, Object... args) {
    }

    @Override public void e(Throwable t, String message, Object... args) {
    }
  }
}
