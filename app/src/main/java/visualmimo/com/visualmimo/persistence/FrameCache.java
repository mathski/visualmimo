package visualmimo.com.visualmimo.persistence;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by alexiomota on 9/23/14.
 */
public class FrameCache {

    private static FrameCache singleton;
    private final ReentrantReadWriteLock mRWLock;
    private LinkedList<byte[]> buffer;

    //Private constructor to prevent instantiation in other places other than getInstance()
    private FrameCache() {
        buffer = new LinkedList<byte[]>();
        mRWLock = new ReentrantReadWriteLock();
    }

    public static FrameCache getInstance() {
        if(singleton == null)
            singleton = new FrameCache();
        return singleton;
    }

    public void addFrame(byte[] frame) {
        mRWLock.writeLock().lock();
        try{
            if(buffer.size() == 2)
                buffer.remove(0);

            buffer.add(frame);
        }
        finally{
            mRWLock.writeLock().unlock();
        }
    }

    /**
     *
     * @param recentPosition Accepted values are currently only 0 or 1
     * @return
     */
    public byte[] getRecentFrame(int recentPosition) {
        if( recentPosition > 1 || recentPosition < 0)
            return null;

        mRWLock.readLock().lock();
        try{
            return buffer.get(recentPosition);
        }
        finally {
            mRWLock.readLock().unlock();
        }
    }

    public int size(){
        return buffer.size();
    }
}
