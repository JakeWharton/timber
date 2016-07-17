package timber.log;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Timber timing logger utility
 */
public final class Lumber {

    public static class Splinter {

        private final int mPosition;
        private final long mTime;
        private final String mLabel;

        public int getPosition() {
            return mPosition;
        }

        public long getTime() {
            return mTime;
        }

        public String getLabel() {
            return mLabel;
        }

        public Splinter(long time, String label, int position) {

            mTime = time;
            mLabel = label;
            mPosition = position;
        }
    }

    public static class Split {

        String mLabel = null;
        List<Splinter> mSplinters;

        public Split(String label) {

            mLabel = label;
            mSplinters = new ArrayList<>();
        }

        public void addSplit(String chipLabel) {

            long now = SystemClock.elapsedRealtime();
            Splinter chip = new Splinter(now, chipLabel, mSplinters.size());
            mSplinters.add(chip);
        }

        public void clearSplits() {

            Splinter splinter = mSplinters.remove(0);
            mSplinters.clear();
            mSplinters.add(splinter);
        }

        public List<String> getSplitLogs() {

            List<String> logs = new ArrayList<>();
            logs.add(mLabel + ": begin");
            final Splinter first = mSplinters.get(0);
            Splinter now = first;

            for (int i = 1; i < mSplinters.size(); i++) {
                now = mSplinters.get(i);
                final Splinter prev = mSplinters.get(i - 1);

                logs.add(String.format("%s:      %d ms, %s", mLabel,
                        now.getTime() - prev.getTime(),
                        now.getLabel()));
            }
            logs.add(mLabel + ": end, " + (now.getTime() - first.getTime()) + " ms");
            return logs;
        }
    }

    static final Map<String, Split> WOOD_SPLITS = new ConcurrentHashMap<>();
    static final String NOT_STARTED = "Cannot call '%s'. Split '%s' hasn't been started yet";
    static final String ALREADY_CREATED = "Cannot call '%s'. Split '%s' has been already started";

    public static void dumpV(String key) {
        for (String l : getSplitLogs(key)) {
            Timber.v(l);
        }
    }

    public static void dumpD(String key) {
        for (String l : getSplitLogs(key)) {
            Timber.d(l);
        }
    }

    public static void dumpI(String key) {
        for (String l : getSplitLogs(key)) {
            Timber.i(l);
        }
    }

    public static void dumpW(String key) {
        for (String l : getSplitLogs(key)) {
            Timber.w(l);
        }
    }

    public static void dumpE(String key) {
        for (String l : getSplitLogs(key)) {
            Timber.e(l);
        }
    }

    public static void dumpWTF(String key) {
        for (String l : getSplitLogs(key)) {
            Timber.wtf(l);
        }
    }

    public static void dumpLog(int priority, String key) {
        for (String l : getSplitLogs(key)) {
            Timber.log(priority, l);
        }
    }

    private static List<String> getSplitLogs(String key) {

        synchronized (WOOD_SPLITS) {

            if (WOOD_SPLITS.containsKey(key)) {
                return WOOD_SPLITS.get(key).getSplitLogs();
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED, "getSplitLogs", key));
            }
        }
    }

    public static void startSplit(String key, String name) {

        synchronized (WOOD_SPLITS) {

            if (!WOOD_SPLITS.containsKey(key)) {

                Split split = new Split(name);
                WOOD_SPLITS.put(key, split);
                addSplit(key, name);
            } else {
                throw new IllegalStateException(String.format(ALREADY_CREATED, "startSplit", key));
            }
        }
    }

    public static void addSplit(String key, String label) {

        synchronized (WOOD_SPLITS) {

            if (WOOD_SPLITS.containsKey(key)) {
                WOOD_SPLITS.get(key).addSplit(label);
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED, "addSplit", key));
            }
        }
    }

    public static void resetSplit(String key) {

        synchronized (WOOD_SPLITS) {

            if (WOOD_SPLITS.containsKey(key)) {
                WOOD_SPLITS.get(key).clearSplits();
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED, "resetSplit", key));
            }
        }
    }

    public static Split remove(String key) {

        synchronized (WOOD_SPLITS) {

            if (WOOD_SPLITS.containsKey(key)) {
                return WOOD_SPLITS.remove(key);
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED, "remove", key));
            }
        }
    }

    public static void clear() {

        synchronized (WOOD_SPLITS) {

            WOOD_SPLITS.clear();
        }
    }

    private Lumber() {
        throw new AssertionError("No instances.");
    }
}
