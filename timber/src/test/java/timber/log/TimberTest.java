package timber.log;

import android.util.Log;
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
import static org.robolectric.shadows.ShadowLog.LogItem;
import static timber.log.Timber.DebugTree.formatString;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE)
public class TimberTest {
  @Before @After public void setUpAndTearDown() {
    Timber.FOREST.clear();
    Timber.TAGGED_TREES.clear();
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

  @Test
  public void testLogExceptionFromSpawnedThread() throws Exception {
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
}
