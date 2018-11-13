package com.example.timber;

import android.app.Application;
import android.util.Log;
import org.jetbrains.annotations.Nullable;
import timber.log.LogcatTree;
import timber.log.Timber;
import timber.log.Tree;

import static timber.log.Timber.INFO;

public class ExampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.INSTANCE.plant(new LogcatTree());
        } else {
            Timber.INSTANCE.plant(new CrashReportingTree());
        }
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static final class CrashReportingTree extends Tree {
        @Override
        public boolean isLoggable(int priority, @Nullable String tag) {
            return priority >= INFO;
        }

        @Override
        protected void performLog(int priority, @Nullable String tag, @Nullable Throwable t, @Nullable String message) {
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
