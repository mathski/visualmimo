package com.android.visualmimo;

import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.android.visualmimo.persistence.FrameCache;
import com.android.visualmimo.persistence.MIMOFrame;

import java.util.List;

/**
 * Utility class for logic concerning proccessing of frames and NDK interface.
 */
public class FrameProcessing {
    static {
        System.loadLibrary("ndk1");
    }

    /** NDK: subtracts frame1 from frame2, overwriting frame1 */
    private static native boolean[] frameSubtraction(
            byte[] frame1, byte[] frame2, byte[] frame3, byte[] frame4,
            int width, int height, float c0x1, float c0y1, float c1x1,
            float c1y1, float c2x1, float c2y1, float c3x1, float c3y1,
            float c0x2, float c0y2, float c1x2, float c1y2, float c2x2,
            float c2y2, float c3x2, float c3y2, float c0x3, float c0y3,
            float c1x3, float c1y3, float c2x3, float c2y3, float c3x3,
            float c3y3, float c0x4, float c0y4, float c1x4, float c1y4,
            float c2x4, float c2y4, float c3x4, float c3y4);


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


                // NDK call: handles subtraction and saving
                boolean[] message = frameSubtraction(
                        frames.get(0).getRaw(),
                        frames.get(1).getRaw(),
                        frames.get(2).getRaw(),
                        frames.get(3).getRaw(),
                        imageWidth,
                        imageHeight,
                        frames.get(0).getCorners()[0][0], frames.get(0).getCorners()[0][1],
                        frames.get(0).getCorners()[1][0], frames.get(0).getCorners()[1][1],
                        frames.get(0).getCorners()[2][0], frames.get(0).getCorners()[2][1],
                        frames.get(0).getCorners()[3][0], frames.get(0).getCorners()[3][1],
                        frames.get(1).getCorners()[0][0], frames.get(1).getCorners()[0][1],
                        frames.get(1).getCorners()[1][0], frames.get(1).getCorners()[1][1],
                        frames.get(1).getCorners()[2][0], frames.get(1).getCorners()[2][1],
                        frames.get(1).getCorners()[3][0], frames.get(1).getCorners()[3][1],
                        frames.get(2).getCorners()[0][0], frames.get(2).getCorners()[0][1],
                        frames.get(2).getCorners()[1][0], frames.get(2).getCorners()[1][1],
                        frames.get(2).getCorners()[2][0], frames.get(2).getCorners()[2][1],
                        frames.get(2).getCorners()[3][0], frames.get(2).getCorners()[3][1],
                        frames.get(3).getCorners()[0][0], frames.get(3).getCorners()[0][1],
                        frames.get(3).getCorners()[1][0], frames.get(3).getCorners()[1][1],
                        frames.get(3).getCorners()[2][0], frames.get(3).getCorners()[2][1],
                        frames.get(3).getCorners()[3][0], frames.get(3).getCorners()[3][1]);

                MessageUtils.printGrid(message, System.out);
                MessageUtils.printArray(message, System.out);

                String ascii = MessageUtils.parseMessage(message);
                double accuracy = MessageUtils.checkAccuracy(message);
                System.out.println(ascii);
                System.out.println(accuracy);

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
