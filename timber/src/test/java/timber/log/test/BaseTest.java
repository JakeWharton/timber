package timber.log.test;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.robolectric.shadows.ShadowLog;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;

public class BaseTest {

    @Before
    @After
    public void setUpAndTearDown() {
        Timber.uprootAll();
    }

    protected static String repeat(char c, int number) {
        char[] data = new char[number];
        Arrays.fill(data, c);
        return new String(data);
    }

    protected static void assertExceptionLogged(String message, String exceptionClassname) {
        List<ShadowLog.LogItem> logs = ShadowLog.getLogs();
        assertThat(logs).hasSize(1);
        ShadowLog.LogItem log = logs.get(0);
        assertThat(log.type).isEqualTo(Log.ERROR);
        assertThat(log.tag).isEqualTo("TimberTest");
        assertThat(log.msg).startsWith(message);
        assertThat(log.msg).contains(exceptionClassname);
        // We use a low-level primitive that Robolectric doesn't populate.
        assertThat(log.throwable).isNull();
    }

    protected static LogAssert assertLog() {
        return new LogAssert(ShadowLog.getLogs());
    }

    protected static final class LogAssert {
        private final List<ShadowLog.LogItem> items;
        private int index = 0;

        private LogAssert(List<ShadowLog.LogItem> items) {
            this.items = items;
        }

        public LogAssert hasVerboseMessage(String tag, String message) {
            return hasMessage(Log.VERBOSE, tag, message);
        }

        public LogAssert containsVerboseMessage(String message) {
            return containsMessage(Log.VERBOSE, message);
        }

        public LogAssert hasDebugMessage(String tag, String message) {
            return hasMessage(Log.DEBUG, tag, message);
        }

        public LogAssert containsDebugMessage(String message) {
            return containsMessage(Log.DEBUG, message);
        }

        public LogAssert hasInfoMessage(String tag, String message) {
            return hasMessage(Log.INFO, tag, message);
        }

        public LogAssert containsInfoMessage(String message) {
            return containsMessage(Log.INFO, message);
        }

        public LogAssert hasWarnMessage(String tag, String message) {
            return hasMessage(Log.WARN, tag, message);
        }

        public LogAssert containsWarnMessage(String message) {
            return containsMessage(Log.WARN, message);
        }

        public LogAssert hasErrorMessage(String tag, String message) {
            return hasMessage(Log.ERROR, tag, message);
        }

        public LogAssert containsErrorMessage(String message) {
            return containsMessage(Log.ERROR, message);
        }

        public LogAssert hasAssertMessage(String tag, String message) {
            return hasMessage(Log.ASSERT, tag, message);
        }

        public LogAssert hasAssertMessage(String message) {
            return containsMessage(Log.ASSERT, message);
        }

        private LogAssert hasMessage(int priority, String tag, String message) {
            ShadowLog.LogItem item = items.get(index++);
            assertThat(item.type).isEqualTo(priority);
            assertThat(item.tag).isEqualTo(tag);
            assertThat(item.msg).isEqualTo(message);
            return this;
        }

        private LogAssert containsMessage(int priority, String message) {
            ShadowLog.LogItem item = items.get(index++);

            assertEquals(item.type, priority);
            assertTrue(item.msg.contains(message));

            return this;
        }

        public void hasNoMoreMessages() {
            assertThat(items).hasSize(index);
        }
    }
}
