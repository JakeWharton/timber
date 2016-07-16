package timber.log.test;

import android.util.TimingLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import timber.log.Timber;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE)
public class LumberTest extends BaseTest {

    @Before
    @After
    public void setUpAndTearDown() {
        Timber.uprootAll();
    }

//    @Test
//    public void baseTest() {
//
//        Timber.DebugTree tree = new Timber.DebugTree();
//        Timber.plant(tree);
//
//        Object key = new Object();
//        Lumber.startSplit(key,"TRACE");
//        Lumber.addSplit(key,"MIDDLE");
//        Lumber.addSplit(key,"END");
//        Lumber.d(key);
//
//        assertLog()
//                .hasDebugMessage("LumberTest", "START: begin")
//                .hasDebugMessage("LumberTest", "MIDDLE")
//                .hasDebugMessage("LumberTest", "END")
//                .hasNoMoreMessages();
//    }

    @Test
    public void timingLoggerTest() {

        TimingLogger timingLogger = new TimingLogger("LumberTest", "TRACE");
        timingLogger.addSplit("START");
        timingLogger.addSplit("MIDDLE");
        timingLogger.addSplit("END");
        timingLogger.dumpToLog();

        List<ShadowLog.LogItem> logs = ShadowLog.getLogs();
//        assertThat(logs).hasSize(1);
    }
}
