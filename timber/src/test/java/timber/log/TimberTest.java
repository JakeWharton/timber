package timber.log;

import android.util.Log;
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
import static timber.log.Timber.DebugTree.formatString;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE)
public class TimberTest {
  @Before @After public void setUpAndTearDown() {
    Timber.FOREST.clear();
    Timber.TAGGED_TREES.clear();
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

    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(3);
    assertThat(logs.get(0).msg).isEqualTo("First");
    assertThat(logs.get(1).msg).isEqualTo("First");
    assertThat(logs.get(2).msg).isEqualTo("Second");
  }

  @Test public void uprootAllRemovesAll() {
    Timber.DebugTree tree1 = new Timber.DebugTree();
    Timber.DebugTree tree2 = new Timber.DebugTree();
    Timber.plant(tree1);
    Timber.plant(tree2);
    Timber.d("First");
    Timber.uprootAll();
    Timber.d("Second");

    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(2);
    assertThat(logs.get(0).msg).isEqualTo("First");
    assertThat(logs.get(1).msg).isEqualTo("First");
  }

  @Test public void noArgsDoesNotFormat() {
    assertThat(formatString("te%st")).isSameAs("te%st");
  }

  @Test public void debugTagWorks() {
    Timber.plant(new Timber.DebugTree());
    Timber.d("Hello, world!");

    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(1);
    LogItem log = logs.get(0);
    assertThat(log.type).isEqualTo(Log.DEBUG);
    assertThat(log.tag).isEqualTo("TimberTest");
    assertThat(log.msg).isEqualTo("Hello, world!");
    assertThat(log.throwable).isNull();
  }

  @Test public void customTagWorks() {
    Timber.plant(new Timber.DebugTree());
    Timber.tag("Custom").d("Hello, world!");

    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(1);
    LogItem log = logs.get(0);
    assertThat(log.type).isEqualTo(Log.DEBUG);
    assertThat(log.tag).isEqualTo("Custom");
    assertThat(log.msg).isEqualTo("Hello, world!");
    assertThat(log.throwable).isNull();
  }

  @Test public void logWithExceptionHasCorrectTag() {
    Timber.plant(new Timber.DebugTree());
    NullPointerException datThrowable = new NullPointerException();
    Timber.e(datThrowable, "OMFG!");

    assertExceptionLogged("OMFG!", "java.lang.NullPointerException");
  }

  @Test public void testLogExceptionFromSpawnedThread() throws Exception {
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

  @Test public void testLogNullMessageWithThrowable() throws Exception {
    Timber.plant(new Timber.DebugTree());
    final NullPointerException datThrowable = new NullPointerException();
    Timber.e(datThrowable, null);

    assertExceptionLogged("", "java.lang.NullPointerException");
  }

  @Test public void chunkAcrossNewlinesAndLimit() {
    Timber.plant(new Timber.DebugTree());
    Timber.d(repeat('a', 3000) + '\n' + repeat('b', 6000) + '\n' + repeat('c', 3000));

    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(4);
    assertThat(logs.get(0).msg).isEqualTo(repeat('a', 3000));
    assertThat(logs.get(1).msg).isEqualTo(repeat('b', 4000));
    assertThat(logs.get(2).msg).isEqualTo(repeat('b', 2000));
    assertThat(logs.get(3).msg).isEqualTo(repeat('c', 3000));
  }

  @Test public void testLogNullMessageWithoutThrowable() throws Exception {
    Timber.plant(new Timber.DebugTree());
    Timber.d(null);

    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(0);
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

  private static String repeat(char c, int number) {
    char[] data = new char[number];
    Arrays.fill(data, c);
    return new String(data);
  }
}
