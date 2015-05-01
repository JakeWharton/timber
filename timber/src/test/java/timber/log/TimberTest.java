package timber.log;

import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static org.fest.assertions.api.Assertions.assertThat;
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
      @Override protected String createStackElementTag(StackTraceElement element) {
        return super.createStackElementTag(element) + ':' + element.getLineNumber();
      }
    });

    Timber.d("Test");

    assertLog()
        .hasDebugMessage("TimberTest:35", "Test")
        .hasNoMoreMessages();
  }

  @Test public void recursion() {
    Timber.Tree timber = Timber.asTree();
    try {
      Timber.plant(timber);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Cannot plant Timber into itself.");
    }
  }

  @Test public void nullTree() {
    try {
      Timber.plant(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("tree == null");
    }
  }

  @Test public void uprootThrowsIfMissing() {
    try {
      Timber.uproot(new Timber.DebugTree());
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("Cannot uproot tree which is not planted: ");
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

  @Test public void debugTreeCustomTag() {
    Timber.plant(new Timber.DebugTree());
    Timber.tag("Custom").d("Hello, world!");

    assertLog()
        .hasDebugMessage("Custom", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Test public void messageWithException() {
    Timber.plant(new Timber.DebugTree());
    NullPointerException datThrowable = new NullPointerException();
    Timber.e(datThrowable, "OMFG!");

    assertExceptionLogged("OMFG!", "java.lang.NullPointerException");
  }

  @Test public void exceptionFromSpawnedThread() throws InterruptedException {
    Timber.plant(new Timber.DebugTree());
    final NullPointerException datThrowable = new NullPointerException();
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread() {
      @Override public void run() {
        Timber.e(datThrowable, "OMFG!");
        latch.countDown();
      }
    }.run();
    latch.await();
    assertExceptionLogged("OMFG!", "java.lang.NullPointerException");
  }

  @Test public void nullMessageWithThrowable() {
    Timber.plant(new Timber.DebugTree());
    final NullPointerException datThrowable = new NullPointerException();
    Timber.e(datThrowable, null);

    assertExceptionLogged("", "java.lang.NullPointerException");
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
    final List<String> logs = new ArrayList<String>();
    Timber.plant(new Timber.DebugTree() {
      @Override protected void log(int priority, String tag, String message, Throwable t) {
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

  @Test public void formatting() {
    Timber.plant(new Timber.DebugTree());
    Timber.v("Hello, %s!", "World");
    Timber.d("Hello, %s!", "World");
    Timber.i("Hello, %s!", "World");
    Timber.w("Hello, %s!", "World");
    Timber.e("Hello, %s!", "World");

    assertLog()
        .hasVerboseMessage("TimberTest", "Hello, World!")
        .hasDebugMessage("TimberTest", "Hello, World!")
        .hasInfoMessage("TimberTest", "Hello, World!")
        .hasWarnMessage("TimberTest", "Hello, World!")
        .hasErrorMessage("TimberTest", "Hello, World!")
        .hasNoMoreMessages();
  }

  private static String repeat(char c, int number) {
    char[] data = new char[number];
    Arrays.fill(data, c);
    return new String(data);
  }

  private static void assertExceptionLogged(String message, String exceptionClassname) {
    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(1);
    LogItem log = logs.get(0);
    assertThat(log.type).isEqualTo(Log.ERROR);
    assertThat(log.tag).isEqualTo("TimberTest");
    assertThat(log.msg).startsWith(message);
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
