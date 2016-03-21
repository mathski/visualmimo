package com.android.visualmimo.persistence;

import com.android.visualmimo.NDKResult;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Singleton which builds up a message across multiple shots.
 * @author revan
 */
public class MessageCache {

    /**
     * A hardcoded number of messages.
     * TODO: switch to some known "start" pattern to support variable length.
     */
    public static final int NUM_MESSAGES = 9;

//    private int expectingIndex = 0;


//    private ArrayList<NDKResult> messages = new ArrayList<NDKResult>(NUM_MESSAGES);
    private NDKResult[] messages = new NDKResult[NUM_MESSAGES];
    /** Threadsafe, since we access it from the threaded NDK callback. */
//    private LinkedBlockingDeque<NDKResult> messages = new LinkedBlockingDeque<NDKResult>();

    private static MessageCache singleton;
    private MessageCache() {

    }

    public static MessageCache getInstance() {
        if (singleton == null) {
            singleton = new MessageCache();
        }
        return singleton;
    }

    /**
     * Adds a message to the list, only if parity is matching.
     * TODO: instead of dropping a mismatch, assume we missed a single message and leave a gap for it.
     * Next loop around, try to fill gap.
     * @param result and NDKResult returned from the NDK.
     * @return true if successfully added
     */
    public boolean addMessage(NDKResult result) {
        synchronized (messages) {
//            if (expectingIndex != result.index) {
//                System.err.println("Was expecting index " + expectingIndex + " but got index "
//                        + result.index + ". Dropping.");
//                return false;
//            }
            if (messages[result.index] != null) {
                System.err.println("Duplicate index " + result.index + ", dropping.");
                return false;
            }

            if (isReady()) {
                System.err.println("Message cache already ready. Dropping.");
                return false;
            }

//            expectingIndex++;
            messages[result.index] = result;
            return true;
        }
    }

    /** Returns true if all expected messages have been collected. */
    public boolean isReady() {
//        return NUM_MESSAGES <= messages.size();
        for (NDKResult r : messages) {
            if (r == null)
                return false;
        }
        return true;
    }

    /** Spits out total contents of list. */
    public boolean[] assemblePattern() {
        if (messages.length == 0) {
            return new boolean[0];
        }

        boolean[] pattern = new boolean[messages.length * 21];

        int pos = 0;
        for (NDKResult result : messages) {
            System.arraycopy(result.message, 0, pattern, pos, 21);
            pos += 21;
        }

        return pattern;
    }

    /** Returns number of accepted messages in cache. */
    public int size() {
        return messages.length;
    }

}
