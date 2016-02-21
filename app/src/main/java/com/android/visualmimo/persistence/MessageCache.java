package com.android.visualmimo.persistence;

import com.android.visualmimo.NDKResult;

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
    public static final int NUM_MESSAGES = 2;

    private boolean expectingOdd = true;

    /** Threadsafe, since we access it from the threaded NDK callback. */
    private LinkedBlockingDeque<NDKResult> messages = new LinkedBlockingDeque<NDKResult>();

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
        if (expectingOdd != result.isOddFrame) {
            System.err.println("Was expecting " + (expectingOdd ? "odd" : "even")
                    + " frame. Dropping.");
            return false;
        }
        if (isReady()) {
            System.err.println("Message cache already ready. Dropping.");
            return false;
        }

        expectingOdd = !expectingOdd;
        return messages.add(result);
    }

    /** Returns true if all expected messages have been collected. */
    public boolean isReady() {
        return NUM_MESSAGES <= messages.size();
    }

    /** Spits out total contents of list. */
    public boolean[] assemblePattern() {
        if (messages.size() == 0) {
            return new boolean[0];
        }

        boolean[] pattern = new boolean[messages.size() * messages.getFirst().message.length];

        int pos = 0;
        for (NDKResult result : messages) {
            System.arraycopy(result.message, 0, pattern, pos, result.message.length);
            pos += result.message.length;
        }

        return pattern;
    }

    /** Returns number of accepted messages in cache. */
    public int size() {
        return messages.size();
    }

}
