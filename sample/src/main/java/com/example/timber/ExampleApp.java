package com.example.timber;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;
import org.jetbrains.annotations.Nullable;
import timber.log.Timber;
import timber.log.Tree;

import static timber.log.Timber.DebugTree;
import static timber.log.Timber.INFO;

public class ExampleApp extends Application {
  @Override public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new DebugTree());
    } else {
      Timber.plant(new CrashReportingTree());
    }
  }

  /** A tree which logs important information for crash reporting. */
  private static final class CrashReportingTree extends Tree {
    @Override public boolean isLoggable(int priority, @Nullable String tag) {
      return priority >= INFO;
    }

    @Override protected void log(int priority, String tag, Throwable t, String message) {
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
}
