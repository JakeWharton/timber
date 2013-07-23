package timber.log;

import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Logging for lazy people. */
public interface Timber {
  /** Log a debug message with optional format args. */
  void d(String message, Object... args);
  /** Log a debug exception and a message with optional format args. */
  void d(Throwable t, String message, Object... args);
  /** Log an info message with optional format args. */
  void i(String message, Object... args);
  /** Log an info exception and a message with optional format args. */
  void i(Throwable t, String message, Object... args);
  /** Log a warning message with optional format args. */
  void w(String message, Object... args);
  /** Log a warning exception and a message with optional format args. */
  void w(Throwable t, String message, Object... args);
  /** Log an error message with optional format args. */
  void e(String message, Object... args);
  /** Log an error exception and a message with optional format args. */
  void e(Throwable t, String message, Object... args);
  /** Sets a custom tag on the next logged message */
  Timber tag(String tag);

  /** A {@link Timber} for debug builds. Automatically infers the tag from the calling class. */
  Timber DEBUG = new Timber() {
    private final Pattern anonymousClass = Pattern.compile("\\$\\d+$");
    private String tag;

    private String className() {
      String className = Thread.currentThread().getStackTrace()[4].getClassName();
      Matcher m = anonymousClass.matcher(className);
      if (m != null && m.find()) {
        className = m.replaceAll("");
      }
      return className.substring(className.lastIndexOf('.') + 1);
    }

    @Override public void d(String message, Object... args) {
      Log.d(tag == null? className() : tag, String.format(message, args));
      tag = null;
    }

    @Override public void d(Throwable t, String message, Object... args) {
      Log.d(tag == null? className() : tag, String.format(message, args), t);
      tag = null;
    }

    @Override public void i(String message, Object... args) {
      Log.i(tag == null? className() : tag, String.format(message, args));
      tag = null;
    }

    @Override public void i(Throwable t, String message, Object... args) {
      Log.i(tag == null? className() : tag, String.format(message, args), t);
      tag = null;
    }

    @Override public void w(String message, Object... args) {
      Log.w(tag == null? className() : tag, String.format(message, args));
      tag = null;
    }

    @Override public void w(Throwable t, String message, Object... args) {
      Log.w(tag == null? className() : tag, String.format(message, args), t);
      tag = null;
    }

    @Override public void e(String message, Object... args) {
      Log.e(tag == null? className() : tag, String.format(message, args));
      tag = null;
    }

    @Override public void e(Throwable t, String message, Object... args) {
      Log.e(tag == null? className() : tag, String.format(message, args), t);
      tag = null;
    }
    
    @Override public Timber tag(String tag) {
      this.tag = tag;
      return this;
    }
  };

  /** A {@link Timber} for production builds. Is neither seen nor heard. */
  Timber PROD = new Timber() {
    @Override public void d(String message, Object... args) {
    }

    @Override public void d(Throwable t, String message, Object... args) {
    }

    @Override public void i(String message, Object... args) {
    }

    @Override public void i(Throwable t, String message, Object... args) {
    }

    @Override public void w(String message, Object... args) {
    }

    @Override public void w(Throwable t, String message, Object... args) {
    }

    @Override public void e(String message, Object... args) {
    }

    @Override public void e(Throwable t, String message, Object... args) {
    }
  };
}
