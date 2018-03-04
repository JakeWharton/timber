package com.example.timber;

import java.util.Map;

/** Not a real crash reporting library! */
public final class FakeCrashLibrary {
  public static void log(int priority, String tag, String message) {
    // TODO add log entry to circular buffer.
  }

  public static void log(int priority, String message, Map<String, Object> metadata) {
    // TODO add log entry with metadata
  }

  public static void logWarning(Throwable t) {
    // TODO report non-fatal warning.
  }

  public static void logError(Throwable t) {
    // TODO report non-fatal error.
  }

  private FakeCrashLibrary() {
    throw new AssertionError("No instances.");
  }
}
