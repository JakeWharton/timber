package timber.log.test;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import timber.log.Lumber;
import timber.log.Timber;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE)
public class LumberTest extends BaseTest {

    @Before
    public void setUp() {
        Timber.uprootAll();

        Timber.DebugTree tree = new Timber.DebugTree();
        Timber.plant(tree);
    }

    @After
    public void tearDown() {
        Timber.uprootAll();
        Lumber.clear();
    }

    @Test
    public void baseUsage() {

        Lumber.startSplit("KEY", "TRACE");
        Lumber.addSplit("KEY", "MIDDLE");
        Lumber.addSplit("KEY", "END");
        Lumber.dumpLog(Log.DEBUG, "KEY");

        assertLog()
                .containsDebugMessage("LumberTest", "TRACE: begin")
                .containsDebugMessage("LumberTest", "MIDDLE")
                .containsDebugMessage("LumberTest", "END")
                .containsDebugMessage("LumberTest", "TRACE: end")
                .hasNoMoreMessages();
    }

    @Test
    public void multipleKeysAndPrio() {

        Lumber.startSplit("KEY1", "TRACE 1");
        Lumber.startSplit("KEY2", "TRACE 2");
        Lumber.startSplit("KEY3", "TRACE 3");

        Lumber.addSplit("KEY1", "START 1");
        Lumber.addSplit("KEY2", "START 2");
        Lumber.addSplit("KEY3", "START 3");

        Lumber.addSplit("KEY1", "END 1");
        Lumber.addSplit("KEY2", "END 2");
        Lumber.addSplit("KEY3", "END 3");

        Lumber.dumpLog(Log.DEBUG, "KEY1");
        Lumber.dumpLog(Log.VERBOSE, "KEY2");
        Lumber.dumpLog(Log.WARN, "KEY3");

        assertLog()
//                .debugLogs()
                .containsDebugMessage("LumberTest", "TRACE 1: begin")
                .containsDebugMessage("LumberTest", "START 1")
                .containsDebugMessage("LumberTest", "END 1")
                .containsDebugMessage("LumberTest", "TRACE 1: end")
                .containsVerboseMessage("LumberTest", "TRACE 2: begin")
                .containsVerboseMessage("LumberTest", "START 2")
                .containsVerboseMessage("LumberTest", "END 2")
                .containsVerboseMessage("LumberTest", "TRACE 2: end")
                .containsWarnMessage("LumberTest", "TRACE 3: begin")
                .containsWarnMessage("LumberTest", "START 3")
                .containsWarnMessage("LumberTest", "END 3")
                .containsWarnMessage("LumberTest", "TRACE 3: end")
                .hasNoMoreMessages();
    }

    @Test
    public void resetSplit() {

        Lumber.startSplit("KEY", "TRACE");
        Lumber.addSplit("KEY", "1");
        Lumber.addSplit("KEY", "2");
        Lumber.addSplit("KEY", "3");
        Lumber.addSplit("KEY", "4");
        Lumber.addSplit("KEY", "5");
        Lumber.addSplit("KEY", "6");
        Lumber.addSplit("KEY", "END");
        Lumber.resetSplit("KEY");
        Lumber.dumpD("KEY");

        assertLog()
//                .debugLogs()
                .containsDebugMessage("LumberTest", "TRACE: begin")
                .containsDebugMessage("LumberTest", "TRACE: end")
                .hasNoMoreMessages();
    }

    @Test
    public void addWithoutStart() {

        try {
            Lumber.addSplit("KEY", "TRACE");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Cannot call 'addSplit'. Split 'KEY' hasn't been started yet");
        }
    }

    @Test
    public void removeSplit() {

        try {
            Lumber.startSplit("KEY", "TRACE");
            Lumber.addSplit("KEY", "TRACE 1");
            Lumber.addSplit("KEY", "TRACE 2");
            Lumber.remove("KEY");
            Lumber.addSplit("KEY", "TRACE 3");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Cannot call 'addSplit'. Split 'KEY' hasn't been started yet");
        }
    }

    @Test
    public void sameKeyStart() {

        try {
            Lumber.startSplit("KEY", "TRACE");
            Lumber.startSplit("KEY", "TRACE");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Cannot call 'startSplit'. "
                    + "Split 'KEY' has been already started");
        }
    }
}
