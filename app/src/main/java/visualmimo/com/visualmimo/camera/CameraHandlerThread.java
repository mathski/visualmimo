package visualmimo.com.visualmimo.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by alexiomota on 9/23/14.
 */
public class CameraHandlerThread extends HandlerThread {
    private static final String TAG = "CameraHandlerThread";
    Handler handler = null;

    public CameraHandlerThread() {
        super("CameraHandlerThread");
        handler = new Handler(getLooper());
    }

    synchronized void notifyCameraOpened() {
        notify();
    }

    void openCamera() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Camera camera = Camera.open(1);
                    notifyCameraOpened();
                }
                catch (RuntimeException e) {
                    Log.e(TAG, "failed to open front camera");
                }
            }
        });

        try {
            wait();
        }
        catch (InterruptedException e) {
            Log.w(TAG, "wait was interrupted");
        }
    }
}
