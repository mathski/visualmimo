package com.android.visualmimo;

/**
 * We need to return multiple values from the NDK.
 * Any changes here have to be updated in the C++ code too.
 */
public class NDKResult {
    public int index;
    public boolean[] message;

    public NDKResult(int index, boolean[] message) {
        this.index = index;
        this.message = message;
    }
}
