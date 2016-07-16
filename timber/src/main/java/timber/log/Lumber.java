package timber.log;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/** Timber timing logger utility */
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

    static final Map<Object, Split> WOOD_SPLITS = new ConcurrentHashMap<Object, Split>();
    static final String NOT_STARTED_FMT = "Cannot call '%s'. Split '%s' hasn't been started yet";

    public static void d(Object key) {

        for (String l : getSplitLogs(key)) {
            Timber.d(l);
        }
    }

    private static List<String> getSplitLogs(Object key) {

        List<String> logs = new ArrayList<>();
        synchronized (WOOD_SPLITS) {

            if (WOOD_SPLITS.containsKey(key)) {
                logs  = WOOD_SPLITS.get(key).getSplitLogs();
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED_FMT,
                        "getSplitLogs", key.toString()));
            }
        }
        return logs;
    }

    public static void startSplit(Object key, String name) {

        synchronized (WOOD_SPLITS) {

            if (!WOOD_SPLITS.containsKey(key)) {

                Split split = new Split(name);
                WOOD_SPLITS.put(key, split);
                addSplit(key, name);
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED_FMT,
                        "startSplit", key.toString()));
            }
        }
    }

    public static void addSplit(Object key, String label) {

        synchronized (WOOD_SPLITS) {

            if (WOOD_SPLITS.containsKey(key)) {
                WOOD_SPLITS.get(key).addSplit(label);
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED_FMT,
                        "addSplit", key.toString()));
            }
        }
    }

    public static void clear(Object key) {

        synchronized (WOOD_SPLITS) {

            if (WOOD_SPLITS.containsKey(key)) {
                WOOD_SPLITS.get(key).clearSplits();
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED_FMT,
                        "clear", key.toString()));
            }
        }
    }

    public static Split remove(Object key) {

        synchronized (WOOD_SPLITS) {

            if (WOOD_SPLITS.containsKey(key)) {
                return WOOD_SPLITS.remove(key);
            } else {
                throw new IllegalStateException(String.format(NOT_STARTED_FMT,
                        "remove", key.toString()));
            }
        }
    }

    private Lumber() {
        throw new AssertionError("No instances.");
    }
}
