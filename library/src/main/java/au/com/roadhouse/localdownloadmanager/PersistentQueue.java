package au.com.roadhouse.localdownloadmanager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;

import timber.log.Timber;

public class PersistentQueue<E extends Serializable> implements Queue<E> {

    private static final String TAG = "PersistentQueue";

    private Queue<E> mInMemoryQueue;
    private final UpdateHandler mUpdateHandler;
    private File mFile;
    private CountDownLatch mFlushLatch;
    private boolean mIsBlocking = false;

    public PersistentQueue(@NonNull File file) {
        HandlerThread thread = new HandlerThread("PersistentQueue");
        thread.start();
        mFile = file;

        loadQueueIntoMemory();
        Looper updateLooper = thread.getLooper();
        mUpdateHandler = new UpdateHandler(updateLooper);
    }

    @Override
    public int size() {
        return mInMemoryQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return mInMemoryQueue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return mInMemoryQueue.contains(o);
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return mInMemoryQueue.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mInMemoryQueue.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] a) {
        return mInMemoryQueue.toArray(a);
    }

    @Override
    public boolean add(E e) {
        if(mInMemoryQueue.add(e)){
            updateFileStore();
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {

        if(mInMemoryQueue.remove(o)){
            updateFileStore();
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return mInMemoryQueue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        //Modifier
        return mInMemoryQueue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        //Modifier
        return mInMemoryQueue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        //Modifier
        return mInMemoryQueue.retainAll(c);
    }

    @Override
    public void clear() {
        mInMemoryQueue.clear();
        updateFileStore();
    }

    @Override
    public boolean offer(E e) {
        if (mInMemoryQueue.offer(e)) {
            updateFileStore();
            return true;
        }
        return false;
    }

    @Override
    public E remove() {
        E item = mInMemoryQueue.remove();
        updateFileStore();

        return item;
    }

    @Override
    public E poll() {
        updateFileStore();
        return mInMemoryQueue.poll();
    }

    private void updateFileStore() {
        if(mIsBlocking){
            updatePersistentStore();
        } else {
            mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage());
        }
    }

    @Override
    public E element() {
        return mInMemoryQueue.element();
    }

    @Override
    public E peek() {
        return mInMemoryQueue.peek();
    }

    private void loadQueueIntoMemory(){
        if(mFile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(mFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                //noinspection unchecked
                mInMemoryQueue = (Queue<E>) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                mInMemoryQueue = new PriorityBlockingQueue<>();
            }
        } else {
            mInMemoryQueue = new PriorityBlockingQueue<>();
        }
    }

    public void setBlocking(boolean isBlocking) {
        mIsBlocking = isBlocking;
    }

    public void flushUpdates() {
        while(mUpdateHandler.pendingCount > 0){
            try {
                mFlushLatch.await();
            } catch (InterruptedException e) {
                Timber.d("flushUpdates: thread was interrupted while waiting, reaquiring latch");
            }
        }
    }

    private final class UpdateHandler extends Handler {
        private int pendingCount = 0;

        UpdateHandler(Looper looper) {
            super(looper);
        }

        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            if(pendingCount == 0){
                mFlushLatch = new CountDownLatch(1);
            }
            pendingCount++;
            return super.sendMessageAtTime(msg, uptimeMillis);
        }

        @Override
        public void dispatchMessage(Message msg) {
            pendingCount --;
            super.dispatchMessage(msg);
            if(pendingCount == 0){
                mFlushLatch.countDown();
            }

        }

        @Override
        public void handleMessage(Message msg) {
            updatePersistentStore();
        }
    }

    private void updatePersistentStore() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(mInMemoryQueue);
            Timber.d(TAG, "handleMessage: Updated persistent storage");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
