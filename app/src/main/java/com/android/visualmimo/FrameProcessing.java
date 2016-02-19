package com.android.visualmimo;

import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.android.visualmimo.persistence.FrameCache;
import com.android.visualmimo.persistence.MIMOFrame;
import com.android.visualmimo.persistence.MessageCache;

import java.util.List;

/**
 * Utility class for logic concerning proccessing of frames and NDK interface.
 */
public class FrameProcessing {
    static {
        System.loadLibrary("ndk1");
    }

    /** NDK: subtracts frame1 from frame2, overwriting frame1 */
    private static native Object frameSubtraction(
            byte[][] frames,
            int width,
            int height,
            float[][] corners);


    public static void processFrames(
            FrameCache cache,
            Handler.Callback callback,
            final int saveCount,
            final boolean benchingInProgress,
            final boolean burstMode,
            final int numSaves,
            final int imageWidth,
            final int imageHeight,
            final double[] accuracies) {
        // this handler will update the UI with the message below
        final Handler handler = new Handler(callback);

        final int index = saveCount;

        final List<MIMOFrame> frames = cache.getBufferFrames();

        // this thread will extract the message
        new Thread(new Runnable() {
            public void run() {
                // perform operations in NDK

                //reorder args into primitive arrays
                byte[][] frameArray = new byte[frames.size()][];
                for (int i = 0; i < frames.size(); i++) {
                    frameArray[i] = frames.get(i).getRaw();
                }

                float[][] corners = new float[frames.size()][9];
                for (int i = 0; i < frames.size(); i++) {
                    float[][] c1 = frames.get(i).getCorners();
                    corners[i][0] = c1[0][0];
                    corners[i][1] = c1[0][1];
                    corners[i][2] = c1[1][0];
                    corners[i][3] = c1[1][1];
                    corners[i][4] = c1[2][0];
                    corners[i][5] = c1[2][1];
                    corners[i][6] = c1[3][0];
                    corners[i][7] = c1[3][1];
                }

                // NDK call: handles subtraction and saving
                NDKResult ndkResult = (NDKResult) frameSubtraction(
                        frameArray,
                        imageWidth,
                        imageHeight,
                        corners);
                boolean[] message = ndkResult.message;

                MessageUtils.printGrid(message, System.out);
                MessageUtils.printArray(message, System.out);

                String ascii = MessageUtils.parseMessage(message);
                double accuracy = MessageUtils.checkAccuracy(message);
                System.out.println(ascii);
                System.out.println(accuracy);
                System.out.println("Parity: " + (ndkResult.isOddFrame ? "odd" : "even"));

                MessageCache cache = MessageCache.getInstance();
                cache.addMessage(ndkResult);
                System.out.println("MessageCache.isReady(): " + cache.isReady());
                System.out.println(MessageUtils.parseMessage(cache.assemblePattern()));
                MessageUtils.printArray(cache.assemblePattern(), System.out);

                // update UI
                if (!benchingInProgress) {
                    if (burstMode) {
                        if (index < numSaves) {
                            accuracies[index - 1] = accuracy;
                        }

                        if (index == numSaves) {
                            double average = 0;
                            for (double a : accuracies) {
                                average += a;
                            }
                            average /= accuracies.length;

                            Message msg = new Message();
                            msg.obj = new ExtractedMessage(average, "average", message);
                            handler.sendMessage(msg);
                        }
                    } else {
                        Message msg = new Message();
                        msg.obj = new ExtractedMessage(accuracy, ascii, message);
                        handler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

}
