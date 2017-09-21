package com.example.timber;

/** Not a real crash reporting library! */
public final class FakeTextFileLogger {
    public static void log(int priority, String tag, String message) {
        // TODO implement saving an error message to a text file on the device
    }

    private FakeTextFileLogger() {
        throw new AssertionError("No instances.");
    }
}
