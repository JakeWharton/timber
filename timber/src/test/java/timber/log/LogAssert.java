package timber.log;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class LogAssert {
  private final List<LogItem> items;
  private int index = 0;

  public LogAssert(List<LogItem> items) {
    this.items = items;
  }

  public LogAssert hasVerboseMessage(String tag, String message) {
    return hasMessage(Timber.VERBOSE, tag, message, null);
  }

  public LogAssert hasDebugMessage(String tag, String message) {
    return hasMessage(Timber.DEBUG, tag, message, null);
  }

  public LogAssert hasInfoMessage(String tag, String message) {
    return hasMessage(Timber.INFO, tag, message, null);
  }

  public LogAssert hasWarnMessage(String tag, String message) {
    return hasMessage(Timber.WARN, tag, message, null);
  }

  public LogAssert hasErrorMessage(String tag, String message) {
    return hasMessage(Timber.ERROR, tag, message, null);
  }

  public LogAssert hasErrorMessage(String tag, String message, Throwable throwable) {
    return hasMessage(Timber.ERROR, tag, message, throwable);
  }

  public LogAssert hasMessage(int priority, String tag, String message, Throwable throwable) {
    LogItem item = getNextLogItem();

    assertThat(item.type).isEqualTo(priority);
    assertThat(item.tag).isEqualTo(tag);
    assertThat(item.msg).isEqualTo(message);
    assertThat(item.throwable).isEqualTo(throwable);

    return this;
  }

  public void hasNoMoreMessages() {
    assertThat(items).hasSize(index);
  }

  public LogItem getNextLogItem() {
    return items.get(index++);
  }
}
