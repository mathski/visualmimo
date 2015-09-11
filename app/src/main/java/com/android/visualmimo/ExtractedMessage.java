package com.android.visualmimo;

/**
 * For sending data from {{com.android.visualmimo.FrameProcessing}} back to {{com.android.visualmimo.MainActivity}}.
 */
public class ExtractedMessage {
    public Double accuracy;
    public String message;
    public String binary;

    public ExtractedMessage(Double accuracy, String message, boolean[] binary) {
        this.accuracy = accuracy;
        this.message = message;
        this.binary = MessageUtils.parseMessageToBinary(binary);
    }

    public String toString() {
        return accuracy + ":" + message;
    }
}
