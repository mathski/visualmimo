package com.android.visualmimo.persistence;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.util.Pair;

/**
 * Singleton that keeps track of the n most recent MIMOFrames.
 * @author alexio
 */
public class FrameCache {

    private static FrameCache singleton;
    private final ReentrantReadWriteLock mRWLock;
    private static final int BUFFER_SIZE = 4;
    private LinkedList<MIMOFrame> buffer;
    private volatile boolean changed;

    //Private constructor to prevent instantiation in other places other than getInstance()
    private FrameCache() {
        buffer = new LinkedList<MIMOFrame>();
        mRWLock = new ReentrantReadWriteLock();
    }

    public static FrameCache getInstance() {
        if(singleton == null)
            singleton = new FrameCache();
        return singleton;
    }

    public void addFrame(byte[] frameData, int width,  int height, float[][] corners) {
        mRWLock.writeLock().lock();
        try{
            if(buffer.size() == BUFFER_SIZE)
                buffer.remove(0);

            buffer.add(new MIMOFrame(frameData, width, height, corners));
            changed = true;
        }
        finally{
            mRWLock.writeLock().unlock();
        }
    }

    /**
     *
     * @param recentPosition
     * @return A buffered frame
     */
    public MIMOFrame getRecentFrame(int recentPosition) {
        if (recentPosition >= buffer.size()) {
            //Log.e("*","Return null");
            return null;
        }

        mRWLock.readLock().lock();
        try {
            //Log.e("*", "SIZE: " + buffer.size() + " POS: " + recentPosition);
            return buffer.get(recentPosition);
        } finally {
            mRWLock.readLock().unlock();
        }
    }
    
    /**
     * @return the BUFFER_SIZE most recent frames.
     * Needed because calling getRecentFrame() twice isn't atomic.
     */
    public List<MIMOFrame> getBufferFrames() {
    	while (buffer.size() < BUFFER_SIZE) {
    		//BUSY LOCK: BAD (TODO)
    		System.out.println("Attempted to get frames before buffer filled.");
    	}
    	
    	mRWLock.readLock().lock();
    	try {
    		return (List<MIMOFrame>) buffer.clone();
    	} finally {
    		mRWLock.readLock().unlock();
    	}
    }

    public int size(){
        return buffer.size();
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged() {
        changed = false;
    }
}

