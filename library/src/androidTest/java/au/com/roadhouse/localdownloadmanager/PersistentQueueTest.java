package au.com.roadhouse.localdownloadmanager;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PersistentQueueTest {

    @Test
    public void testBlockingPersistence() throws Exception{
        File queueFile = new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "test.que");
        queueFile.delete();
        PersistentQueue<String> persistentQueue = new PersistentQueue<>(queueFile);
        persistentQueue.setBlocking(true);
        persistentQueue.add("test");
        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 1);
        assertEquals(persistentQueue.peek(), "test");
        queueFile.delete();
    }

    @Test
    public void testNonBlockingPersistence() throws Exception{
        File queueFile = new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "test.que");
        queueFile.delete();
        PersistentQueue<String> persistentQueue = new PersistentQueue<>(queueFile);
        persistentQueue.setBlocking(false);
        persistentQueue.add("test");
        persistentQueue.flushUpdates();
        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 1);
        assertEquals(persistentQueue.peek(), "test");
        queueFile.delete();
    }

    @Test
    public void testItemRemoval() {
        File queueFile = new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "test.que");
        queueFile.delete();
        PersistentQueue<String> persistentQueue = new PersistentQueue<>(queueFile);
        persistentQueue.add("Hello");
        persistentQueue.add("You");
        assertEquals(persistentQueue.size(), 2);
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 2);
        persistentQueue.remove("Hello");
        assertEquals(persistentQueue.size(), 1);
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 1);

    }

    @Test
    public void testRemove() {
        File queueFile = new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "test.que");
        queueFile.delete();
        PersistentQueue<String> persistentQueue = new PersistentQueue<>(queueFile);
        persistentQueue.add("Hello");
        persistentQueue.add("You");
        assertEquals(persistentQueue.size(), 2);
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 2);
       ;
        assertEquals(persistentQueue.remove(), "Hello");
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 1);
        assertEquals(persistentQueue.remove(), "You");

        queueFile.delete();
        persistentQueue = new PersistentQueue<>(queueFile);
        //Sanity check
        assertEquals(persistentQueue.size(), 0);
        try{
            persistentQueue.remove();
        } catch (Exception e){
            return;
        }

        //Shouldn't get here
        assertTrue(false);

    }

    @Test
    public void testPeek() {
        File queueFile = new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "test.que");
        queueFile.delete();
        PersistentQueue<String> persistentQueue = new PersistentQueue<>(queueFile);
        persistentQueue.add("Hello");
        persistentQueue.add("You");
        assertEquals(persistentQueue.size(), 2);
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 2);

        assertEquals(persistentQueue.peek(), "Hello");
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 2);
        assertEquals(persistentQueue.peek(), "Hello");

    }

    @Test
    public void testElement() {
        File queueFile = new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "test.que");
        queueFile.delete();
        PersistentQueue<String> persistentQueue = new PersistentQueue<>(queueFile);
        persistentQueue.add("Hello");
        persistentQueue.add("You");
        assertEquals(persistentQueue.size(), 2);
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 2);

        assertEquals(persistentQueue.element(), "Hello");
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 2);
        assertEquals(persistentQueue.element(), "Hello");

        queueFile.delete();
        persistentQueue = new PersistentQueue<>(queueFile);
        //Sanity check
        assertEquals(persistentQueue.size(), 0);
        try{
            persistentQueue.element();
        } catch (Exception e){
            return;
        }

        //Shouldn't get here
        assertTrue(false);
    }

    @Test
    public void testClear() {
        File queueFile = new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "test.que");
        queueFile.delete();
        PersistentQueue<String> persistentQueue = new PersistentQueue<>(queueFile);
        persistentQueue.add("Hello");
        persistentQueue.add("You");
        assertEquals(persistentQueue.size(), 2);
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 2);
        persistentQueue.clear();
        assertEquals(persistentQueue.size(), 0);
        persistentQueue.flushUpdates();

        persistentQueue = new PersistentQueue<>(queueFile);
        assertEquals(persistentQueue.size(), 0);

    }
}
