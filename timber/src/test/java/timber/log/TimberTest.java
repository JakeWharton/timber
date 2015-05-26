package timber.log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class TimberTest {
  private static List<LogItem> logs = new ArrayList<LogItem>();

  @Before @After public void setUpAndTearDown() {
    Timber.uprootAll();
    logs.clear();
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
      Timber.uproot(new TestTree());
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("Cannot uproot tree which is not planted: ");
    }
  }

  @Test public void uprootRemovesTree() {
    TestTree tree1 = new TestTree();
    TestTree tree2 = new TestTree();
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
    TestTree tree1 = new TestTree();
    TestTree tree2 = new TestTree();
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

  private static LogAssert assertLog() {
    return new LogAssert(logs);
  }

  private static class TestTree extends Timber.Tree {
    @Override protected void log(int priority, String tag, String message, Throwable t) {
      logs.add(new LogItem(priority, tag, message, t));
    }

    @Override public String getTag() {
      String tag = super.getTag();

      if (tag != null) {
        return tag;
      }

      return "TimberTest";
    }
  }
}
