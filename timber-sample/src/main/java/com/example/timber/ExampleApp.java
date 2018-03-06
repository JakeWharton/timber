package com.example.timber;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Map;

import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class ExampleApp extends Application {
  @Override public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new DebugTree());
    } else {
      Timber.plant(new CrashReportingTree());
      Timber.plant(new StructuredLoggingTree());
    }
  }

  /** A tree which logs important information for crash reporting. */
  private static class CrashReportingTree extends Timber.Tree {
    @Override protected void log(int priority, String tag, @NonNull String message, Throwable t) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG) {
        return;
      }

      FakeCrashLibrary.log(priority, tag, message);

      if (t != null) {
        if (priority == Log.ERROR) {
          FakeCrashLibrary.logError(t);
        } else if (priority == Log.WARN) {
          FakeCrashLibrary.logWarning(t);
        }
      }
    }
  }

  /** A tree which logs important events in a structured format. */
  private static class StructuredLoggingTree extends Timber.Tree {
    @Override protected void log(int priority, String tag, @NonNull String message, Throwable t) {
      log(priority, tag, message, t, null);
    }

    @Override protected void log(int priority, String tag, @NonNull String message, Throwable t,
                                 Map<String, Object> metadata) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG) {
        return;
      }

      FakeCrashLibrary.log(priority, message, metadata);
    }
  }
}
