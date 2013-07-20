package timber.log;

import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Logging for lazy people. */
public interface Timber {
  /** Prefix to add to all log messages. Assumes that this prefix is shorter (by a good deal) than the max log tag
     * length */
  protected static final String _LOG_PREFIX = "base_";
  /** Precomputed length of the log prefix */
  protected static final int _LOG_PREFIX_LENGTH = _LOG_PREFIX.length();
  /** Length at which to begin truncating log tags */
  protected static final int _MAX_LOG_TAG_LENGTH = 23;
  
  /* Automatic tag variates */
  /** Log a debug message with optional format args. */
  void autoTaggedD(String message, Object... args);
  /** Log a debug exception and a message with optional format args. */
  void autoTaggedD(Throwable t, String message, Object... args);
  /** Log an info message with optional format args. */
  void autoTaggedI(String message, Object... args);
  /** Log an info exception and a message with optional format args. */
  void autoTaggedI(Throwable t, String message, Object... args);
  /** Log a warning message with optional format args. */
  void autoTaggedW(String message, Object... args);
  /** Log a warning exception and a message with optional format args. */
  void autoTaggedW(Throwable t, String message, Object... args);
  /** Log an error message with optional format args. */
  void autoTaggedE(String message, Object... args);
  /** Log an error exception and a message with optional format args. */
  void autoTaggedE(Throwable t, String message, Object... args);
  
  /* Manual tag variates */
  /** Log a debug message with optional format args. */
  void d(String tag, String message, Object... args);
  /** Log a debug exception and a message with optional format args. */
  void d(String tag, Throwable t, String message, Object... args);
  /** Log an info message with optional format args. */
  void i(String tag, String message, Object... args);
  /** Log an info exception and a message with optional format args. */
  void i(String tag, Throwable t, String message, Object... args);
  /** Log a warning message with optional format args. */
  void w(String tag, String message, Object... args);
  /** Log a warning exception and a message with optional format args. */
  void w(String tag, Throwable t, String message, Object... args);
  /** Log an error message with optional format args. */
  void e(String tag, String message, Object... args);
  /** Log an error exception and a message with optional format args. */
  void e(String tag, Throwable t, String message, Object... args);

  /** A {@link Timber} for debug builds. Automatically infers the tag from the calling class. */
  Timber DEBUG = new Timber() {
    private final Pattern anonymousClass = Pattern.compile("\\$\\d+$");

    private String className() {
      String className = Thread.currentThread().getStackTrace()[4].getClassName();
      Matcher m = anonymousClass.matcher(className);
      if (m != null && m.find()) {
        className = m.replaceAll("");
      }
      return className.substring(className.lastIndexOf('.') + 1);
    }
    
    /**
     * <p>Constructs an appropriate tag string based on the given tag. Prefixes a log prefix to the tag, while
     * guaranteeing that the tag doesn't get too long.</p>
     * @param tag the given tag
     * @return the constructed tag
     */
    public static String makeLogTag(String tag) {
        return tag.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH
                ? LOG_PREFIX + tag.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1)
                : LOG_PREFIX + tag;
    }

    @Override public void autoTaggedD(String message, Object... args) {
      Log.d(className(), String.format(message, args));
    }

    @Override public void autoTaggedD(Throwable t, String message, Object... args) {
      Log.d(className(), String.format(message, args), t);
    }

    @Override public void autoTaggedI(String message, Object... args) {
      Log.i(className(), String.format(message, args));
    }

    @Override public void autoTaggedI(Throwable t, String message, Object... args) {
      Log.i(className(), String.format(message, args), t);
    }

    @Override public void autoTaggedW(String message, Object... args) {
      Log.w(className(), String.format(message, args));
    }

    @Override public void autoTaggedW(Throwable t, String message, Object... args) {
      Log.w(className(), String.format(message, args), t);
    }

    @Override public void autoTaggedE(String message, Object... args) {
      Log.e(className(), String.format(message, args));
    }

    @Override public void autoTaggedE(Throwable t, String message, Object... args) {
      Log.e(className(), String.format(message, args), t);
    }
    
    @Override public void d(String tag, String message, Object... args) {
      Log.d(tag, String.format(message, args));
    }

    @Override public void d(String tag, Throwable t, String message, Object... args) {
      Log.d(tag, String.format(message, args), t);
    }

    @Override public void i(String tag, String message, Object... args) {
      Log.i(tag, String.format(message, args));
    }

    @Override public void i(String tag, Throwable t, String message, Object... args) {
      Log.i(tag, String.format(message, args), t);
    }

    @Override public void w(String tag, String message, Object... args) {
      Log.w(w, String.format(message, args));
    }

    @Override public void w(String tag, Throwable t, String message, Object... args) {
      Log.w(tag, String.format(message, args), t);
    }

    @Override public void e(String tag, String message, Object... args) {
      Log.e(tag, String.format(message, args));
    }

    @Override public void e(String tag, Throwable t, String message, Object... args) {
      Log.e(tag, String.format(message, args), t);
    }
  };

  /** A {@link Timber} for production builds. Is neither seen nor heard. */
  Timber PROD = new Timber() {
    @Override public void autoTaggedD(String message, Object... args) {
    }

    @Override public void autoTaggedD(Throwable t, String message, Object... args) {
    }

    @Override public void autoTaggedI(String message, Object... args) {
    }

    @Override public void autoTaggedI(Throwable t, String message, Object... args) {
    }

    @Override public void autoTaggedW(String message, Object... args) {
    }

    @Override public void autoTaggedW(Throwable t, String message, Object... args) {
    }

    @Override public void autoTaggedE(String message, Object... args) {
    }

    @Override public void autoTaggedE(Throwable t, String message, Object... args) {
    }
    
    @Override public void d(String tag, String message, Object... args) {
    }

    @Override public void d(String tag, Throwable t, String message, Object... args) {
    }

    @Override public void i(String tag, String message, Object... args) {
    }

    @Override public void i(String tag, Throwable t, String message, Object... args) {
    }

    @Override public void w(String tag, String message, Object... args) {
    }

    @Override public void w(String tag, Throwable t, String message, Object... args) {
    }

    @Override public void e(String tag, String message, Object... args) {
    }

    @Override public void e(String tag, Throwable t, String message, Object... args) {
    }
  };
}
