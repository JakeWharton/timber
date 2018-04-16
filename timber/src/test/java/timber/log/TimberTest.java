package timber.log;

import android.os.Build;
import android.util.Log;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.shadows.ShadowLog.LogItem;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE)
public class TimberTest {
  @Before @After public void setUpAndTearDown() {
    Timber.uprootAll();
  }

  // NOTE: This class references the line number. Keep it at the top so it does not change.
  @Test public void debugTreeCanAlterCreatedTag() {
    Timber.plant(new Timber.DebugTree() {
      @Override protected String createStackElementTag(@NotNull StackTraceElement element) {
        return super.createStackElementTag(element) + ':' + element.getLineNumber();
      }
    });

    Timber.d("Test");

    assertLog()
        .hasDebugMessage("TimberTest:41", "Test")
        .hasNoMoreMessages();
  }

  @Test public void recursion() {
    Timber.Tree timber = Timber.asTree();
    try {
      Timber.plant(timber);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("Cannot plant Timber into itself.");
    }
    try {
      Timber.plant(new Timber.Tree[]{timber});
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("Cannot plant Timber into itself.");
    }
  }

  @Test public void treeCount() {
    // inserts trees and checks if the amount of returned trees matches.
    assertThat(Timber.treeCount()).isEqualTo(0);
    for (int i = 1; i < 50; i++) {
      Timber.plant(new Timber.DebugTree());
      assertThat(Timber.treeCount()).isEqualTo(i);
    }
    Timber.uprootAll();
    assertThat(Timber.treeCount()).isEqualTo(0);
  }

  @SuppressWarnings("ConstantConditions")
  @Test public void nullTree() {
    Timber.Tree nullTree = null;
    try {
      Timber.plant(nullTree);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("tree == null");
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Test public void nullTreeArray() {
    Timber.Tree[] nullTrees = null;
    try {
      Timber.plant(nullTrees);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("trees == null");
    }
    nullTrees = new Timber.Tree[]{null};
    try {
      Timber.plant(nullTrees);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("trees contains null");
    }
  }

  @Test public void forestReturnsAllPlanted() {
    Timber.DebugTree tree1 = new Timber.DebugTree();
    Timber.DebugTree tree2 = new Timber.DebugTree();
    Timber.plant(tree1);
    Timber.plant(tree2);

    assertThat(Timber.forest()).containsExactly(tree1, tree2);
  }

  @Test public void forestReturnsAllTreesPlanted() {
    Timber.DebugTree tree1 = new Timber.DebugTree();
    Timber.DebugTree tree2 = new Timber.DebugTree();
    Timber.plant(tree1, tree2);

    assertThat(Timber.forest()).containsExactly(tree1, tree2);
  }

  @Test public void uprootThrowsIfMissing() {
    try {
      Timber.uproot(new Timber.DebugTree());
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().startsWith("Cannot uproot tree which is not planted: ");
    }
  }

  @Test public void uprootRemovesTree() {
    Timber.DebugTree tree1 = new Timber.DebugTree();
    Timber.DebugTree tree2 = new Timber.DebugTree();
    Timber.plant(tree1);
    Timber.plant(tree2);
    Timber.d("First");
    Timber.uproot(tree1);
    Timber.d("Second");

    assertLog()
        .hasDebugMessage("TimberTest", "First")
        .hasDebugMessage("TimberTest", "First")
        .hasDebugMessage("TimberTest", "Second")
        .hasNoMoreMessages();
  }

  @Test public void uprootAllRemovesAll() {
    Timber.DebugTree tree1 = new Timber.DebugTree();
    Timber.DebugTree tree2 = new Timber.DebugTree();
    Timber.plant(tree1);
    Timber.plant(tree2);
    Timber.d("First");
    Timber.uprootAll();
    Timber.d("Second");

    assertLog()
        .hasDebugMessage("TimberTest", "First")
        .hasDebugMessage("TimberTest", "First")
        .hasNoMoreMessages();
  }

  @Test public void noArgsDoesNotFormat() {
    Timber.plant(new Timber.DebugTree());
    Timber.d("te%st");

    assertLog()
        .hasDebugMessage("TimberTest", "te%st")
        .hasNoMoreMessages();
  }

  @Test public void debugTreeTagGeneration() {
    Timber.plant(new Timber.DebugTree());
    Timber.d("Hello, world!");

    assertLog()
        .hasDebugMessage("TimberTest", "Hello, world!")
        .hasNoMoreMessages();
  }

  class ThisIsAReallyLongClassName {
    void run() {
      Timber.d("Hello, world!");
    }
  }

  @Config(sdk = 23)
  @Test public void debugTreeTagTruncation() {
    Timber.plant(new Timber.DebugTree());

    new ThisIsAReallyLongClassName().run();

    assertLog()
        .hasDebugMessage("TimberTest$ThisIsAReall", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Config(sdk = 24)
  @Test public void debugTreeTagNoTruncation() {
    Timber.plant(new Timber.DebugTree());

    new ThisIsAReallyLongClassName().run();

    assertLog()
        .hasDebugMessage("TimberTest$ThisIsAReallyLongClassName", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Test public void debugTreeTagGenerationStripsAnonymousClassMarker() {
    Timber.plant(new Timber.DebugTree());
    new Runnable() {
      @Override public void run() {
        Timber.d("Hello, world!");

        new Runnable() {
          @Override public void run() {
            Timber.d("Hello, world!");
          }
        }.run();
      }
    }.run();

    assertLog()
        .hasDebugMessage("TimberTest", "Hello, world!")
        .hasDebugMessage("TimberTest", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Test public void debugTreeGeneratedTagIsLoggable() {
    Timber.plant(new Timber.DebugTree() {
      private static final int MAX_TAG_LENGTH = 23;

      @Override protected void log(int priority, String tag, @NotNull String message, Throwable t) {
        try {
          assertTrue(Log.isLoggable(tag, priority));
          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            assertTrue(tag.length() <= MAX_TAG_LENGTH);
          }
        } catch (IllegalArgumentException e) {
          fail(e.getMessage());
        }
        super.log(priority, tag, message, t);
      }
    });
    class ClassNameThatIsReallyReallyReallyLong {
      {
        Timber.i("Hello, world!");
      }
    }
    new ClassNameThatIsReallyReallyReallyLong();
    assertLog()
        .hasInfoMessage("TimberTest$1ClassNameTh", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Test public void debugTreeCustomTag() {
    Timber.plant(new Timber.DebugTree());
    Timber.tag("Custom").d("Hello, world!");

    assertLog()
        .hasDebugMessage("Custom", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Test public void messageWithException() {
    Timber.plant(new Timber.DebugTree());
    NullPointerException datThrowable = truncatedThrowable(NullPointerException.class);
    Timber.e(datThrowable, "OMFG!");

    assertExceptionLogged(Log.ERROR, "OMFG!", "java.lang.NullPointerException");
  }

  @Test public void exceptionOnly() {
    Timber.plant(new Timber.DebugTree());

    Timber.v(truncatedThrowable(IllegalArgumentException.class));
    assertExceptionLogged(Log.VERBOSE, null, "java.lang.IllegalArgumentException", "TimberTest", 0);

    Timber.i(truncatedThrowable(NullPointerException.class));
    assertExceptionLogged(Log.INFO, null, "java.lang.NullPointerException", "TimberTest", 1);

    Timber.d(truncatedThrowable(UnsupportedOperationException.class));
    assertExceptionLogged(Log.DEBUG, null, "java.lang.UnsupportedOperationException", "TimberTest", 2);

    Timber.w(truncatedThrowable(UnknownHostException.class));
    assertExceptionLogged(Log.WARN, null, "java.net.UnknownHostException", "TimberTest", 3);

    Timber.e(truncatedThrowable(ConnectException.class));
    assertExceptionLogged(Log.ERROR, null, "java.net.ConnectException", "TimberTest", 4);

    Timber.wtf(truncatedThrowable(AssertionError.class));
    assertExceptionLogged(Log.ASSERT, null, "java.lang.AssertionError", "TimberTest", 5);
  }

  @Test public void exceptionOnlyCustomTag() {
    Timber.plant(new Timber.DebugTree());

    Timber.tag("Custom").v(truncatedThrowable(IllegalArgumentException.class));
    assertExceptionLogged(Log.VERBOSE, null, "java.lang.IllegalArgumentException", "Custom", 0);

    Timber.tag("Custom").i(truncatedThrowable(NullPointerException.class));
    assertExceptionLogged(Log.INFO, null, "java.lang.NullPointerException", "Custom", 1);

    Timber.tag("Custom").d(truncatedThrowable(UnsupportedOperationException.class));
    assertExceptionLogged(Log.DEBUG, null, "java.lang.UnsupportedOperationException", "Custom", 2);

    Timber.tag("Custom").w(truncatedThrowable(UnknownHostException.class));
    assertExceptionLogged(Log.WARN, null, "java.net.UnknownHostException", "Custom", 3);

    Timber.tag("Custom").e(truncatedThrowable(ConnectException.class));
    assertExceptionLogged(Log.ERROR, null, "java.net.ConnectException", "Custom", 4);

    Timber.tag("Custom").wtf(truncatedThrowable(AssertionError.class));
    assertExceptionLogged(Log.ASSERT, null, "java.lang.AssertionError", "Custom", 5);
  }

  @Test public void exceptionFromSpawnedThread() throws InterruptedException {
    Timber.plant(new Timber.DebugTree());
    final NullPointerException datThrowable = truncatedThrowable(NullPointerException.class);
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread() {
      @Override public void run() {
        Timber.e(datThrowable, "OMFG!");
        latch.countDown();
      }
    }.start();
    latch.await();
    assertExceptionLogged(Log.ERROR, "OMFG!", "java.lang.NullPointerException");
  }

  @Test public void nullMessageWithThrowable() {
    Timber.plant(new Timber.DebugTree());
    NullPointerException datThrowable = truncatedThrowable(NullPointerException.class);
    Timber.e(datThrowable, null);

    assertExceptionLogged(Log.ERROR, "", "java.lang.NullPointerException");
  }

  @Test public void chunkAcrossNewlinesAndLimit() {
    Timber.plant(new Timber.DebugTree());
    Timber.d(repeat('a', 3000) + '\n' + repeat('b', 6000) + '\n' + repeat('c', 3000));

    assertLog()
        .hasDebugMessage("TimberTest", repeat('a', 3000))
        .hasDebugMessage("TimberTest", repeat('b', 4000))
        .hasDebugMessage("TimberTest", repeat('b', 2000))
        .hasDebugMessage("TimberTest", repeat('c', 3000))
        .hasNoMoreMessages();
  }

  @Test public void nullMessageWithoutThrowable() {
    Timber.plant(new Timber.DebugTree());
    Timber.d(null);

    assertLog().hasNoMoreMessages();
  }

  @Test public void logMessageCallback() {
    final List<String> logs = new ArrayList<>();
    Timber.plant(new Timber.DebugTree() {
      @Override protected void log(int priority, String tag, @NotNull String message, Throwable t) {
        logs.add(priority + " " + tag + " " + message);
      }
    });

    Timber.v("Verbose");
    Timber.tag("Custom").v("Verbose");
    Timber.d("Debug");
    Timber.tag("Custom").d("Debug");
    Timber.i("Info");
    Timber.tag("Custom").i("Info");
    Timber.w("Warn");
    Timber.tag("Custom").w("Warn");
    Timber.e("Error");
    Timber.tag("Custom").e("Error");
    Timber.wtf("Assert");
    Timber.tag("Custom").wtf("Assert");

    assertThat(logs).containsExactly( //
        "2 TimberTest Verbose", //
        "2 Custom Verbose", //
        "3 TimberTest Debug", //
        "3 Custom Debug", //
        "4 TimberTest Info", //
        "4 Custom Info", //
        "5 TimberTest Warn", //
        "5 Custom Warn", //
        "6 TimberTest Error", //
        "6 Custom Error", //
        "7 TimberTest Assert", //
        "7 Custom Assert" //
    );
  }

  @Test public void logAtSpecifiedPriority() {
    Timber.plant(new Timber.DebugTree());

    Timber.log(Log.VERBOSE, "Hello, World!");
    Timber.log(Log.DEBUG, "Hello, World!");
    Timber.log(Log.INFO, "Hello, World!");
    Timber.log(Log.WARN, "Hello, World!");
    Timber.log(Log.ERROR, "Hello, World!");
    Timber.log(Log.ASSERT, "Hello, World!");

    assertLog()
        .hasVerboseMessage("TimberTest", "Hello, World!")
        .hasDebugMessage("TimberTest", "Hello, World!")
        .hasInfoMessage("TimberTest", "Hello, World!")
        .hasWarnMessage("TimberTest", "Hello, World!")
        .hasErrorMessage("TimberTest", "Hello, World!")
        .hasAssertMessage("TimberTest", "Hello, World!")
        .hasNoMoreMessages();
  }

  @Test public void formatting() {
    Timber.plant(new Timber.DebugTree());
    Timber.v("Hello, %s!", "World");
    Timber.d("Hello, %s!", "World");
    Timber.i("Hello, %s!", "World");
    Timber.w("Hello, %s!", "World");
    Timber.e("Hello, %s!", "World");
    Timber.wtf("Hello, %s!", "World");

    assertLog()
        .hasVerboseMessage("TimberTest", "Hello, World!")
        .hasDebugMessage("TimberTest", "Hello, World!")
        .hasInfoMessage("TimberTest", "Hello, World!")
        .hasWarnMessage("TimberTest", "Hello, World!")
        .hasErrorMessage("TimberTest", "Hello, World!")
        .hasAssertMessage("TimberTest", "Hello, World!")
        .hasNoMoreMessages();
  }

  @SuppressWarnings("deprecation") // Explicitly testing deprecated variant.
  @Test public void isLoggableControlsLogging() {
    Timber.plant(new Timber.DebugTree() {
      @Override protected boolean isLoggable(int priority) {
        return priority == Log.INFO;
      }
    });
    Timber.v("Hello, World!");
    Timber.d("Hello, World!");
    Timber.i("Hello, World!");
    Timber.w("Hello, World!");
    Timber.e("Hello, World!");
    Timber.wtf("Hello, World!");

    assertLog()
        .hasInfoMessage("TimberTest", "Hello, World!")
        .hasNoMoreMessages();
  }

  @Test public void isLoggableTagControlsLogging() {
    Timber.plant(new Timber.DebugTree() {
      @Override protected boolean isLoggable(String tag, int priority) {
        return "FILTER".equals(tag);
      }
    });
    Timber.tag("FILTER").v("Hello, World!");
    Timber.d("Hello, World!");
    Timber.i("Hello, World!");
    Timber.w("Hello, World!");
    Timber.e("Hello, World!");
    Timber.wtf("Hello, World!");

    assertLog()
        .hasVerboseMessage("FILTER", "Hello, World!")
        .hasNoMoreMessages();
  }

  @Test public void logsUnknownHostExceptions() {
    Timber.plant(new Timber.DebugTree());
    Timber.e(truncatedThrowable(UnknownHostException.class), null);

    assertExceptionLogged(Log.ERROR, "", "UnknownHostException");
  }

  @Test public void tagIsClearedWhenNotLoggable() {
    Timber.plant(new Timber.DebugTree() {
      @Override
      protected boolean isLoggable(String tag, int priority) {
        return priority >= Log.WARN;
      }
    });
    Timber.tag("NotLogged").i("Message not logged");
    Timber.w("Message logged");

    assertLog()
        .hasWarnMessage("TimberTest", "Message logged")
        .hasNoMoreMessages();
  }

  @Test public void logsWithCustomFormatter() {
    Timber.plant(new Timber.DebugTree() {
      @Override
      protected String formatMessage(@NotNull String message, @NotNull Object[] args) {
        return String.format("Test formatting: " + message, args);
      }
    });
    Timber.d("Test message logged. %d", 100);

    assertLog()
        .hasDebugMessage("TimberTest", "Test formatting: Test message logged. 100");
  }

  @Test public void nullArgumentObjectArray() {
    Timber.plant(new Timber.DebugTree());
    Timber.v("Test", (Object[]) null);
    assertLog()
        .hasVerboseMessage("TimberTest", "Test")
        .hasNoMoreMessages();
  }

  private static <T extends Throwable> T truncatedThrowable(Class<T> throwableClass) {
    try {
      T throwable = throwableClass.newInstance();
      StackTraceElement[] stackTrace = throwable.getStackTrace();
      int traceLength = stackTrace.length > 5 ? 5 : stackTrace.length;
      throwable.setStackTrace(Arrays.copyOf(stackTrace, traceLength));
      return throwable;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static String repeat(char c, int number) {
    char[] data = new char[number];
    Arrays.fill(data, c);
    return new String(data);
  }

  private static void assertExceptionLogged(int logType, String message, String exceptionClassname) {
    assertExceptionLogged(logType, message, exceptionClassname, null, 0);
  }

  private static void assertExceptionLogged(int logType, String message, String exceptionClassname, String tag,
                                            int index) {
    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(index + 1);
    LogItem log = logs.get(index);
    assertThat(log.type).isEqualTo(logType);
    assertThat(log.tag).isEqualTo(tag != null ? tag : "TimberTest");

    if (message != null) {
      assertThat(log.msg).startsWith(message);
    }

    assertThat(log.msg).contains(exceptionClassname);
    // We use a low-level primitive that Robolectric doesn't populate.
    assertThat(log.throwable).isNull();
  }

  private static LogAssert assertLog() {
    return new LogAssert(ShadowLog.getLogs());
  }

  private static final class LogAssert {
    private final List<LogItem> items;
    private int index = 0;

    private LogAssert(List<LogItem> items) {
      this.items = items;
    }

    public LogAssert hasVerboseMessage(String tag, String message) {
      return hasMessage(Log.VERBOSE, tag, message);
    }

    public LogAssert hasDebugMessage(String tag, String message) {
      return hasMessage(Log.DEBUG, tag, message);
    }

    public LogAssert hasInfoMessage(String tag, String message) {
      return hasMessage(Log.INFO, tag, message);
    }

    public LogAssert hasWarnMessage(String tag, String message) {
      return hasMessage(Log.WARN, tag, message);
    }

    public LogAssert hasErrorMessage(String tag, String message) {
      return hasMessage(Log.ERROR, tag, message);
    }

    public LogAssert hasAssertMessage(String tag, String message) {
      return hasMessage(Log.ASSERT, tag, message);
    }

    private LogAssert hasMessage(int priority, String tag, String message) {
      LogItem item = items.get(index++);
      assertThat(item.type).isEqualTo(priority);
      assertThat(item.tag).isEqualTo(tag);
      assertThat(item.msg).isEqualTo(message);
      return this;
    }

    public void hasNoMoreMessages() {
      assertThat(items).hasSize(index);
    }
  }
}
