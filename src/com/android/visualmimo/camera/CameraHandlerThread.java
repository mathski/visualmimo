package com.android.visualmimo.camera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

/**
 * Used to have camera return frames in a seperate thread
 */
public class CameraHandlerThread extends HandlerThread {
    private static final String TAG = "CameraHandlerThread";
    Handler handler = null;

    public CameraHandlerThread() {
        super("CameraHandlerThread");
        start();
        handler = new Handler(getLooper());
    }

    synchronized void notifyCameraOpened() {
        notify();
    }

    public void openCamera(final SurfaceHolder holder, final Context context) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    try{
                        camera.setPreviewDisplay(holder);
                        setCameraOrientation(context, camera, Camera.CameraInfo.CAMERA_FACING_BACK);
                    } catch(IOException ex) {
                        Log.d(TAG, "Error setting camera preview: " + ex.getMessage());
                    }
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
    
    public  void setCameraOrientation(Context context, Camera camera, int cameraId) {
        if(camera == null)
            return;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        WindowManager window = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = window.getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result = (info.orientation - degrees + 360) % 360;
        camera.setDisplayOrientation(result);
    }
}
