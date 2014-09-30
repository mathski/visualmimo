package visualmimo.com.visualmimo.persistence;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FrameCache {

    private static FrameCache singleton;
    private final ReentrantReadWriteLock mRWLock;
    //Current implementation only stores 2 frames
    private LinkedList<Frame> buffer;
    private boolean changed;

    //Private constructor to prevent instantiation in other places other than getInstance()
    private FrameCache() {
        buffer = new LinkedList<Frame>();
        mRWLock = new ReentrantReadWriteLock();
    }

    public static FrameCache getInstance() {
        if(singleton == null)
            singleton = new FrameCache();
        return singleton;
    }

    public void addFrame(byte[] frameData, int width,  int height) {
        mRWLock.writeLock().lock();
        try{
            if(buffer.size() == 2)
                buffer.remove(0);

            buffer.add(new Frame(frameData, width, height));
        }
        finally{
            mRWLock.writeLock().unlock();
        }
    }

    /**
     *
     * @param recentPosition Accepted values are currently only 0 or 1
     * @return A buffered frame
     */
    public Frame getRecentFrame(int recentPosition) {
        if( recentPosition >= buffer.size()) {
            //Log.e("*","Return null");
            return null;
        }

        mRWLock.readLock().lock();
        try{
            //Log.e("*", "SIZE: " + buffer.size() + " POS: " + recentPosition);
            return buffer.get(recentPosition);
        }
        finally {
            mRWLock.readLock().unlock();
        }
    }

    public int size(){
        return buffer.size();
    }

    public boolean isChanged() {
        return changed;
    }
}
