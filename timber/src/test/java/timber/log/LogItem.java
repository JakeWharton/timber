package timber.log;

public class LogItem {
  public final int type;
  public final String tag;
  public final String msg;
  public final Throwable throwable;

  public LogItem(int type, String tag, String msg, Throwable throwable) {
    this.type = type;
    this.tag = tag;
    this.msg = msg;
    this.throwable = throwable;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LogItem log = (LogItem) o;
    return type == log.type
        && !(msg != null ? !msg.equals(log.msg) : log.msg != null)
        && !(tag != null ? !tag.equals(log.tag) : log.tag != null)
        && !(throwable != null ? !throwable.equals(log.throwable) : log.throwable != null);
  }

  @Override public int hashCode() {
    int result = type;
    result = 31 * result + (tag != null ? tag.hashCode() : 0);
    result = 31 * result + (msg != null ? msg.hashCode() : 0);
    result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return "LogItem{" +
        "type=" + type +
        ", tag='" + tag + '\'' +
        ", msg='" + msg + '\'' +
        ", throwable=" + throwable +
        '}';
  }
}
