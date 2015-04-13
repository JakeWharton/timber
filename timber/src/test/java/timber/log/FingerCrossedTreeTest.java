package timber.log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FingerCrossedTreeTest {

    @Mock Timber.Tree tree;
    private FingerCrossedTree fingerCrossedTree;
    private Object obj;
    private Throwable t;

    @Before
    public void setUp() throws Exception {
        fingerCrossedTree = new FingerCrossedTree(tree, FingerCrossedTree.LogLevel.WARNING);
        obj = new Object();
        t = new Throwable();
    }

    @Test
    public void actionLevelReached() throws Exception {
        fingerCrossedTree.w("warning", obj);
        fingerCrossedTree.w(t, "warning", obj);
        fingerCrossedTree.e("error", obj);
        fingerCrossedTree.e(t, "error", obj);
        verify(tree).w("warning", obj);
        verify(tree).w(t, "warning", obj);
        verify(tree).e("error", obj);
        verify(tree).e(t, "error", obj);
    }

    @Test
    public void actionLevelNotReached() throws Exception {
        fingerCrossedTree.v("verbose", obj);
        fingerCrossedTree.v(t, "verbose", obj);
        fingerCrossedTree.i("info", obj);
        fingerCrossedTree.i(t, "info", obj);
        fingerCrossedTree.d("debug", obj);
        fingerCrossedTree.d(t, "debug", obj);
        verify(tree, never()).v("verbose", obj);
        verify(tree, never()).v(t, "verbose", obj);
        verify(tree, never()).i("info", obj);
        verify(tree, never()).i(t, "info", obj);
        verify(tree, never()).d("debug", obj);
        verify(tree, never()).d(t, "debug", obj);
    }

    @Test
    public void passPreviousLogs() throws Exception {
        InOrder inOrder = inOrder(tree);

        fingerCrossedTree.v("verbose", obj);
        fingerCrossedTree.i(t, "info", obj);
        fingerCrossedTree.e("error", obj);
        inOrder.verify(tree).v("verbose", obj);
        inOrder.verify(tree).i(t, "info", obj);
        inOrder.verify(tree).e("error", obj);
    }

    @Test
    public void passPreviousLogsOnce() throws Exception {
        fingerCrossedTree.v("verbose", obj);
        fingerCrossedTree.i(t, "info", obj);
        fingerCrossedTree.e("error", obj);

        reset(tree);

        fingerCrossedTree.e("anotherError", obj);
        verify(tree, never()).v("verbose", obj);
        verify(tree, never()).i(t, "info", obj);
        verify(tree, never()).e("error", obj);
    }
}
