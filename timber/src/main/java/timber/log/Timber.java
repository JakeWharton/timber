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

  /** Set a one-time tag for use on the next logging call. */
  public static Tree tag(String tag) {
    for (int index = 0, size = TAGGED_TREES.size(); index < size; index++) {
      ((TaggedTree) FOREST.get(TAGGED_TREES.keyAt(index))).tag(tag);
    }
    return TREE_OF_SOULS;
  }

  /** Add a new logging tree. */
  public static void plant(Tree tree) {
    if (tree instanceof TaggedTree) {
      TAGGED_TREES.append(FOREST.size(), true);
    }
    FOREST.add(tree);
  }

  static final List<Tree> FOREST = new CopyOnWriteArrayList<Tree>();
  static final SparseBooleanArray TAGGED_TREES = new SparseBooleanArray();

  /** A {@link Tree} that delegates to all planted trees in the {@link #FOREST forest}. */
  private static final Tree TREE_OF_SOULS = new Tree() {
    @Override public void v(String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.v(message, args);
      }
    }

    @Override public void v(Throwable t, String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.v(t, message, args);
      }
    }

    @Override public void d(String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.d(message, args);
      }
    }

    @Override public void d(Throwable t, String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.d(t, message, args);
      }
    }

    @Override public void i(String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.i(message, args);
      }
    }

    @Override public void i(Throwable t, String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.i(t, message, args);
      }
    }

    @Override public void w(String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.w(message, args);
      }
    }

    @Override public void w(Throwable t, String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.w(t, message, args);
      }
    }

    @Override public void e(String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.e(message, args);
      }
    }

    @Override public void e(Throwable t, String message, Object... args) {
      for (Tree tree : FOREST) {
        tree.e(t, message, args);
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
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<String>();

    private static String createTag() {
      String tag = NEXT_TAG.get();
      if (tag != null) {
        NEXT_TAG.remove();
        return tag;
      }

      tag = new Throwable().getStackTrace()[4].getClassName();
      Matcher m = ANONYMOUS_CLASS.matcher(tag);
      if (m.find()) {
        tag = m.replaceAll("");
      }
      return tag.substring(tag.lastIndexOf('.') + 1);
    }

    private static String formatString(String message, Object... args) {
      // If no varargs are supplied, treat it as a request to log the string without formatting.
      return args.length == 0 ? message : String.format(message, args);
    }

    @Override public void v(String message, Object... args) {
      Log.v(createTag(), formatString(message, args));
    }

    @Override public void v(Throwable t, String message, Object... args) {
      Log.v(createTag(), formatString(message, args), t);
    }

    @Override public void d(String message, Object... args) {
      Log.d(createTag(), String.format(message, args));
    }

    @Override public void d(Throwable t, String message, Object... args) {
      Log.d(createTag(), formatString(message, args), t);
    }

    @Override public void i(String message, Object... args) {
      Log.i(createTag(), formatString(message, args));
    }

    @Override public void i(Throwable t, String message, Object... args) {
      Log.i(createTag(), formatString(message, args), t);
    }

    @Override public void w(String message, Object... args) {
      Log.w(createTag(), formatString(message, args));
    }

    @Override public void w(Throwable t, String message, Object... args) {
      Log.w(createTag(), formatString(message, args), t);
    }

    @Override public void e(String message, Object... args) {
      Log.e(createTag(), formatString(message, args));
    }

    @Override public void e(Throwable t, String message, Object... args) {
      Log.e(createTag(), formatString(message, args), t);
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
