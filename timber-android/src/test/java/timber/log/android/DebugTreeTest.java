package timber.log.android;

import android.util.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import timber.log.LogAssert;
import timber.log.LogItem;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE)
public class DebugTreeTest {
  @Before @After public void setUpAndTearDown() {
    Timber.uprootAll();
  }

  // NOTE: This class references the line number. Keep it at the top so it does not change.
  @Test public void debugTreeCanAlterCreatedTag() {
    Timber.plant(new DebugTree() {
      @Override protected String createStackElementTag(StackTraceElement element) {
        return super.createStackElementTag(element) + ':' + element.getLineNumber();
      }
    });

    Timber.d("Test");

    assertLog()
        .hasDebugMessage("DebugTreeTest:37", "Test") // 37 == Line number for Timber.d("Test")
        .hasNoMoreMessages();
  }

  @Test public void debugTreeTagGeneration() {
    Timber.plant(new DebugTree());
    Timber.d("Hello, world!");

    assertLog()
        .hasDebugMessage("DebugTreeTest", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Test public void debugTreeTagGenerationStripsAnonymousClassMarker() {
    Timber.plant(new DebugTree());
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
        .hasDebugMessage("DebugTreeTest", "Hello, world!")
        .hasDebugMessage("DebugTreeTest", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Test public void debugTreeCustomTag() {
    Timber.plant(new DebugTree());
    Timber.tag("Custom").d("Hello, world!");

    assertLog()
        .hasDebugMessage("Custom", "Hello, world!")
        .hasNoMoreMessages();
  }

  @Test public void messageWithException() {
    Timber.plant(new DebugTree());
    NullPointerException datThrowable = new NullPointerException();
    Timber.e(datThrowable, "OMFG!");

    assertLog()
        .hasErrorMessage("DebugTreeTest", "OMFG!", datThrowable)
        .hasNoMoreMessages();
  }

  @Test public void exceptionFromSpawnedThread() throws InterruptedException {
    Timber.plant(new DebugTree());
    final NullPointerException datThrowable = new NullPointerException();
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread() {
      @Override public void run() {
        Timber.e(datThrowable, "OMFG!");
        latch.countDown();
      }
    }.run();
    latch.await();
    assertLog()
        .hasErrorMessage("DebugTreeTest", "OMFG!", datThrowable)
        .hasNoMoreMessages();
  }

  @Test public void nullMessageWithThrowable() {
    Timber.plant(new DebugTree());
    final NullPointerException datThrowable = new NullPointerException();
    Timber.e(datThrowable, null);

    assertLog()
        .hasErrorMessage("DebugTreeTest", "", datThrowable)
        .hasNoMoreMessages();
  }

  @Test public void chunkAcrossNewlinesAndLimit() {
    Timber.plant(new DebugTree());
    Timber.d(repeat('a', 3000) + '\n' + repeat('b', 6000) + '\n' + repeat('c', 3000));

    assertLog()
        .hasDebugMessage("DebugTreeTest", repeat('a', 3000))
        .hasDebugMessage("DebugTreeTest", repeat('b', 4000))
        .hasDebugMessage("DebugTreeTest", repeat('b', 2000))
        .hasDebugMessage("DebugTreeTest", repeat('c', 3000))
        .hasNoMoreMessages();
  }

  @Test public void nullMessageWithoutThrowable() {
    Timber.plant(new DebugTree());
    Timber.d(null);

    assertLog().hasNoMoreMessages();
  }

  @Test public void logMessageCallback() {
    final List<String> logs = new ArrayList<String>();
    Timber.plant(new DebugTree() {
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

    assertThat(logs).containsExactly(
        "2 DebugTreeTest Verbose", //
        "2 Custom Verbose", //
        "3 DebugTreeTest Debug", //
        "3 Custom Debug", //
        "4 DebugTreeTest Info", //
        "4 Custom Info", //
        "5 DebugTreeTest Warn", //
        "5 Custom Warn", //
        "6 DebugTreeTest Error", //
        "6 Custom Error", //
        "7 DebugTreeTest Assert", //
        "7 Custom Assert" //
    );
  }

  @Test public void isLoggableControlsLogging() {
    Timber.plant(new DebugTree() {
      @Override
      protected boolean isLoggable(int priority) {
        return priority == Log.INFO;
      }
    });
    Timber.v("Hello, World!");
    Timber.d("Hello, World!");
    Timber.i("Hello, World!");
    Timber.w("Hello, World!");
    Timber.e("Hello, World!");

    assertLog()
        .hasInfoMessage("DebugTreeTest", "Hello, World!")
        .hasNoMoreMessages();
  }

  private static String repeat(char c, int number) {
    char[] data = new char[number];
    Arrays.fill(data, c);
    return new String(data);
  }

  private static LogAssert assertLog() {
    return new DebugTreeLogAssert(getLogs());
  }

  private static List<LogItem> getLogs() {
    List<ShadowLog.LogItem> robolectricLogItems = ShadowLog.getLogs();
    List<LogItem> logItems = new ArrayList<LogItem>(robolectricLogItems.size());

    for (ShadowLog.LogItem logItem : robolectricLogItems) {
      logItems.add(new LogItem(logItem.type, logItem.tag, logItem.msg, logItem.throwable));
    }

    return logItems;
  }

  private static class DebugTreeLogAssert extends LogAssert {
    public DebugTreeLogAssert(List<LogItem> items) {
      super(items);
    }

    @Override public LogAssert hasMessage(int priority, String tag, String message, Throwable throwable) {
      LogItem item = getNextLogItem();

      assertThat(item.type).isEqualTo(priority);
      assertThat(item.tag).isEqualTo(tag);

      if (throwable != null) {
        assertThat(item.msg).startsWith(message);
        assertThat(item.msg).contains(throwable.getClass().getName());
        // We use a low-level primitive that Robolectric doesn't populate.
        assertThat(item.throwable).isNull();
      } else {
        assertThat(item.msg).isEqualTo(message);
      }

      return this;
    }
  }
}
