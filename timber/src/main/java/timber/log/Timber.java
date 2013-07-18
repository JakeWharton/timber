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

    @Override public void d(String message, Object... args) {
      Log.d(className(), String.format(message, args));
    }

    @Override public void d(Throwable t, String message, Object... args) {
      Log.e(className(), String.format(message, args), t);
    }

    @Override public void i(String message, Object... args) {
      Log.i(className(), String.format(message, args));
    }

    @Override public void i(Throwable t, String message, Object... args) {
      Log.e(className(), String.format(message, args), t);
    }

    @Override public void w(String message, Object... args) {
      Log.w(className(), String.format(message, args));
    }

    @Override public void w(Throwable t, String message, Object... args) {
      Log.e(className(), String.format(message, args), t);
    }

    @Override public void e(String message, Object... args) {
      Log.e(className(), String.format(message, args));
    }

    @Override public void e(Throwable t, String message, Object... args) {
      Log.e(className(), String.format(message, args), t);
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
