package com.android.visualmimo.persistence;

import com.android.visualmimo.NDKResult;

/**
 * Singleton which builds up a message across multiple shots.
 * @author revan
 */
public class MessageCache {

    /**
     * A hardcoded number of messages.
     * TODO: switch to some known "start" pattern to support variable length.
     */
    public static final int NUM_MESSAGES = 4;
    public static final int MESSAGE_BITS = 4 * 7;
    private int size = 0;


    private final NDKResult[] messages = new NDKResult[NUM_MESSAGES];

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

            if (isReady()) {
                System.err.println("Message cache already ready. Dropping.");
                // TODO: try setting high bits to 0
                return false;
            }

            if (result.mismatches > MESSAGE_BITS) {
                System.err.println("Way too many mismatches. Dropping.");
                return false;
            }

            if (result.index >= NUM_MESSAGES) {
                System.err.println("Index too large: " + result.index);
                return false;
            }

            if (messages[result.index] == null) {
                this.size++;
            } else {
                if (messages[result.index].mismatches > result.mismatches) {
                    System.err.println("Duplicate index " + result.index + ", keeping new result.");
                } else {
                    System.err.println("Duplicate index " + result.index + ", keeping old result.");
                    return false;
                }
            }

            messages[result.index] = result;

            return true;
        }
    }

    /** Returns true if all expected messages have been collected. */
    public boolean isReady() {
        for (NDKResult r : messages) {
            if (r == null)
                return false;
        }
        return true;
    }

    /** Spits out total contents of list. */
    public boolean[] assemblePattern() {
        if (this.size == 0) {
            return new boolean[0];
        }

        boolean[] pattern = new boolean[this.size * MESSAGE_BITS];

        int pos = 0;
        for (NDKResult result : messages) {
            if (result != null) {
                System.arraycopy(result.message, 0, pattern, pos, MESSAGE_BITS);
                pos += MESSAGE_BITS;
            }
        }

        return pattern;
    }

    /** Returns number of accepted messages in cache. */
    public int size() {
        return this.size;
    }

}
