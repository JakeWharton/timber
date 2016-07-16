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

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE)
public class LumberTest extends BaseTest {

    @Before
    @After
    public void setUpAndTearDown() {
        Timber.uprootAll();
    }

    @Test
    public void baseTest() {

        Timber.DebugTree tree = new Timber.DebugTree();
        Timber.plant(tree);

        Object key = new Object();
        Lumber.startSplit(key, "TRACE");
        Lumber.addSplit(key, "MIDDLE");
        Lumber.addSplit(key, "END");
        Lumber.dumpLog(Log.DEBUG, key);

        assertLog()
                .containsDebugMessage("LumberTest", "TRACE: begin")
                .containsDebugMessage("LumberTest", "MIDDLE")
                .containsDebugMessage("LumberTest", "END")
                .containsDebugMessage("LumberTest", "TRACE: end")
                .hasNoMoreMessages();
    }
}
