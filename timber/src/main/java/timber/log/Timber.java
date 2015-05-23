package timber.log;

import timber.log.internal.FastPrintWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Logging for lazy people. */
public final class Timber {
  public static final int VERBOSE = 2;
  public static final int DEBUG = 3;
  public static final int INFO = 4;
  public static final int WARN = 5;
  public static final int ERROR = 6;
  public static final int ASSERT = 7;

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

  /** Log an assert message with optional format args. */
  public static void wtf(String message, Object... args) {
    TREE_OF_SOULS.wtf(message, args);
  }

  /** Log an assert exception and a message with optional format args. */
  public static void wtf(Throwable t, String message, Object... args) {
    TREE_OF_SOULS.wtf(t, message, args);
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
    List<Tree> forest = FOREST;
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0, count = forest.size(); i < count; i++) {
      forest.get(i).explicitTag.set(tag);
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
    FOREST.add(tree);
  }

  /** Remove a planted tree. */
  public static void uproot(Tree tree) {
    if (!FOREST.remove(tree)) {
      throw new IllegalArgumentException("Cannot uproot tree which is not planted: " + tree);
    }
  }

  /** Remove all planted trees. */
  public static void uprootAll() {
    FOREST.clear();
  }

  private static final List<Tree> FOREST = new CopyOnWriteArrayList<Tree>();

  /** A {@link Tree} that delegates to all planted trees in the {@linkplain #FOREST forest}. */
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

    @Override public void wtf(String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).wtf(message, args);
      }
    }

    @Override public void wtf(Throwable t, String message, Object... args) {
      List<Tree> forest = FOREST;
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, count = forest.size(); i < count; i++) {
        forest.get(i).wtf(t, message, args);
      }
    }

    @Override protected void log(int priority, String tag, String message, Throwable t) {
      throw new AssertionError("Missing override for log method.");
    }
  };

  private Timber() {
    throw new AssertionError("No instances.");
  }

  /** A facade for handling logging calls. Install instances via {@link #plant Timber.plant()}. */
  public static abstract class Tree {
    private final ThreadLocal<String> explicitTag = new ThreadLocal<String>();

    public String getTag() {
      String tag = explicitTag.get();
      if (tag != null) {
        explicitTag.remove();
      }
      return tag;
    }

    /** Log a verbose message with optional format args. */
    public void v(String message, Object... args) {
      prepareLog(VERBOSE, null, message, args);
    }

    /** Log a verbose exception and a message with optional format args. */
    public void v(Throwable t, String message, Object... args) {
      prepareLog(VERBOSE, t, message, args);
    }

    /** Log a debug message with optional format args. */
    public void d(String message, Object... args) {
      prepareLog(DEBUG, null, message, args);
    }

    /** Log a debug exception and a message with optional format args. */
    public void d(Throwable t, String message, Object... args) {
      prepareLog(DEBUG, t, message, args);
    }

    /** Log an info message with optional format args. */
    public void i(String message, Object... args) {
      prepareLog(INFO, null, message, args);
    }

    /** Log an info exception and a message with optional format args. */
    public void i(Throwable t, String message, Object... args) {
      prepareLog(INFO, t, message, args);
    }

    /** Log a warning message with optional format args. */
    public void w(String message, Object... args) {
      prepareLog(WARN, null, message, args);
    }

    /** Log a warning exception and a message with optional format args. */
    public void w(Throwable t, String message, Object... args) {
      prepareLog(WARN, t, message, args);
    }

    /** Log an error message with optional format args. */
    public void e(String message, Object... args) {
      prepareLog(ERROR, null, message, args);
    }

    /** Log an error exception and a message with optional format args. */
    public void e(Throwable t, String message, Object... args) {
      prepareLog(ERROR, t, message, args);
    }

    /** Log an assert message with optional format args. */
    public void wtf(String message, Object... args) {
      prepareLog(ASSERT, null, message, args);
    }

    /** Log an assert exception and a message with optional format args. */
    public void wtf(Throwable t, String message, Object... args) {
      prepareLog(ASSERT, t, message, args);
    }

    /** Return whether a message at {@code priority} should be logged. */
    protected boolean isLoggable(int priority) {
      return true;
    }

    private void prepareLog(int priority, Throwable t, String message, Object... args) {
      if (!isLoggable(priority)) {
        return;
      }
      if (message != null && message.length() == 0) {
        message = null;
      }
      if (message == null) {
        if (t == null) {
          return; // Swallow message if it's null and there's no throwable.
        }
        message = getStackTraceString(t);
      } else {
        if (args.length > 0) {
          message = String.format(message, args);
        }
        if (t != null) {
          message += "\n" + getStackTraceString(t);
        }
      }

      log(priority, getTag(), message, t);
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    private static String getStackTraceString(Throwable tr) {
      if (tr == null) {
        return "";
      }

      // This is to reduce the amount of log spew that apps do in the non-error
      // condition of the network being unavailable.
      Throwable t = tr;
      while (t != null) {
        if (t instanceof UnknownHostException) {
          return "";
        }
        t = t.getCause();
      }

      StringWriter sw = new StringWriter();
      PrintWriter pw = new FastPrintWriter(sw, false, 256);
      tr.printStackTrace(pw);
      pw.flush();
      return sw.toString();
    }

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.
     *
     * @param priority Log level.
     * @param tag Explicit or inferred tag. May be {@code null}.
     * @param message Formatted log message. May be {@code null}, but then {@code t} will not be.
     * @param t Accompanying exceptions. May be {@code null}, but then {@code message} will not be.
     */
    protected abstract void log(int priority, String tag, String message, Throwable t);
  }
}
