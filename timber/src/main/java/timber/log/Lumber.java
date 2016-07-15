package timber.log;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/** Timber timing logger utility */
public final class Lumber {

    public static class Woodchip {

        private long mTime;
        private String mLabel;

        public long getTime() {
            return mTime;
        }

        public String getLabel() {
            return mLabel;
        }

        public Woodchip(long time, String label) {

            mTime = time;
            mLabel = label;
        }
    }

    public static class Trace {

        String mLabel = null;
        List<Woodchip> mChips;

        public Trace(String label) {
            mLabel = label;
            mChips = new ArrayList<>();
        }

        public void addTrace(String chipLabel) {

            long now = SystemClock.elapsedRealtime();
            Woodchip chip = new Woodchip(now, chipLabel);
            mChips.add(chip);
        }

        public List<Woodchip> getWoodchips() {

            return mChips;
        }

        public List<String> getTraceLogs() {

            List<String> logs = new ArrayList<>();
            logs.add(mLabel + ": begin");
            final Woodchip first = mChips.get(0);
            Woodchip now = first;

            for (int i = 1; i < mChips.size(); i++) {
                now = mChips.get(i);
                final Woodchip prev = mChips.get(i - 1);

                logs.add(mLabel + ":      " + (now.getTime() - prev.getTime()) + " ms, " + now.getLabel());
            }
            logs.add(mLabel + ": end, " + (now.getTime() - first.getTime()) + " ms");
            return logs;
        }
    }

    static final Map<Object, Trace> WOODCHIPS = new ConcurrentHashMap<Object, Trace>();

    public static void d(Object key) {

        synchronized (WOODCHIPS) {

            if (WOODCHIPS.containsKey(key)) {

                List<String> logs = WOODCHIPS.get(key).getTraceLogs();
                for (String l : logs)
                    Timber.d(l);
            }
        }
    }

    public static void startTrace(Object key, String name) {

        synchronized (WOODCHIPS) {

            if (!WOODCHIPS.containsKey(key)) {

                Trace trace = new Trace(name);
                WOODCHIPS.put(key, trace);
            }
            else
            {
                throw new IllegalStateException(String.format("Cannot call startTrace. Trace for key '%s' has been already started",key.toString()));
            }
        }
    }

    public static void addTrace(Object key, String label) {

        synchronized (WOODCHIPS) {

            if (!WOODCHIPS.containsKey(key)) {

                throw new IllegalStateException(String.format("Cannot call addTrace. Trace for key '%s' hasn't been started yet",key.toString()));
            }

            WOODCHIPS.get(key).addTrace(label);
        }
    }

    private Lumber() {
        throw new AssertionError("No instances.");
    }
}
