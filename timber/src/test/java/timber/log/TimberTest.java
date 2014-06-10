package timber.log;

import android.util.Log;
import java.util.List;
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

    List<LogItem> logs = ShadowLog.getLogs();
    assertThat(logs).hasSize(1);
    LogItem log = logs.get(0);
    assertThat(log.type).isEqualTo(Log.ERROR);
    assertThat(log.tag).isEqualTo("TimberTest");
    assertThat(log.msg).startsWith("OMFG!");
    assertThat(log.msg).contains("java.lang.NullPointerException");
    // We use a low-level primitive that Robolectric doesn't populate.
    assertThat(log.throwable).isNull();
  }
}
