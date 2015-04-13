package timber.log;

import timber.log.Timber.Tree;

import java.util.LinkedList;
import java.util.Queue;

public class FingerCrossedTree implements Tree {

    private final Tree tree;
    private final LogLevel actionLevel;
    private final Queue<LogElement> buffer = new LinkedList<LogElement>();

    public enum LogLevel {
        VERBOSE(0), DEBUG(1), INFO(2), WARNING(3), ERROR(4);

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }
    }

    public FingerCrossedTree(Tree tree, LogLevel actionLevel) {
        this.tree = tree;
        this.actionLevel = actionLevel;
    }

    private void maybeLog(Throwable t, String message, Object[] args, LogLevel threshold) {
        if (thresholdReached(threshold)) {
            dequeueMessages();
            logMessage(t, message, args, threshold);
        } else {
            saveLog(t, message, args, threshold);
        }
    }

    private void dequeueMessages() {
        LogElement el;
        while ((el = buffer.poll()) != null) {
            logMessage(el.t, el.message, el.args, el.logLevel);
        }
    }

    private boolean thresholdReached(LogLevel threshold) {
        return actionLevel.level <= threshold.level;
    }

    private void saveLog(Throwable t, String message, Object[] args, LogLevel logLevel) {
        buffer.offer(new LogElement(t, message, args, logLevel));
    }

    private void logMessage(Throwable t, String message, Object[] args, LogLevel logLevel) {
        switch (logLevel) {
            case VERBOSE:
                if (t == null) {
                    tree.v(message, args);
                } else {
                    tree.v(t, message, args);
                }
                break;
            case DEBUG:
                if (t == null) {
                    tree.d(message, args);
                } else {
                    tree.d(t, message, args);
                }
                break;
            case INFO:
                if (t == null) {
                    tree.i(message, args);
                } else {
                    tree.i(t, message, args);
                }
                break;
            case WARNING:
                if (t == null) {
                    tree.w(message, args);
                } else {
                    tree.w(t, message, args);
                }
                break;
            case ERROR:
                if (t == null) {
                    tree.e(message, args);
                } else {
                    tree.e(t, message, args);
                }
                break;
        }
    }

    @Override
    public void v(String message, Object... args) {
        maybeLog(null, message, args, LogLevel.VERBOSE);
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        maybeLog(t, message, args, LogLevel.VERBOSE);
    }

    @Override
    public void d(String message, Object... args) {
        maybeLog(null, message, args, LogLevel.DEBUG);
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        maybeLog(t, message, args, LogLevel.DEBUG);
    }

    @Override
    public void i(String message, Object... args) {
        maybeLog(null, message, args, LogLevel.INFO);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        maybeLog(t, message, args, LogLevel.INFO);
    }

    @Override
    public void w(String message, Object... args) {
        maybeLog(null, message, args, LogLevel.WARNING);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        maybeLog(t, message, args, LogLevel.WARNING);
    }

    @Override
    public void e(String message, Object... args) {
        maybeLog(null, message, args, LogLevel.ERROR);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        maybeLog(t, message, args, LogLevel.ERROR);
    }

    private static class LogElement {
        public final Throwable t;
        public final String message;
        public final Object[] args;
        public final LogLevel logLevel;

        private LogElement(Throwable t, String message, Object[] args, LogLevel logLevel) {
            this.t = t;
            this.message = message;
            this.args = args;
            this.logLevel = logLevel;
        }
    }
}
