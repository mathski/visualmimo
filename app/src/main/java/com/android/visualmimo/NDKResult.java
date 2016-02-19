package com.android.visualmimo;

/**
 * We need to return multiple values from the NDK.
 * Any changes here have to be updated in the C++ code too.
 */
public class NDKResult {
    public boolean isOddFrame;
    public boolean[] message;

    public NDKResult(boolean isOddFrame, boolean[] message) {
        this.isOddFrame = isOddFrame;
        this.message = message;
    }
}
