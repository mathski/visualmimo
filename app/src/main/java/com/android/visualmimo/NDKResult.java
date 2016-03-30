package com.android.visualmimo;

/**
 * We need to return multiple values from the NDK.
 * Any changes here have to be updated in the C++ code too.
 */
public class NDKResult {
    public int index;
    public int mismatches;
    public boolean[] message;

    public NDKResult(int index, int mismatches, boolean[] message) {
        this.index = index;
        this.mismatches = mismatches;
        this.message = message;
    }
}
