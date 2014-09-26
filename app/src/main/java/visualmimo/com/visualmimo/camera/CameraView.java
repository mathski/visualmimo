package visualmimo.com.visualmimo.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import visualmimo.com.visualmimo.R;
import visualmimo.com.visualmimo.persistence.FrameCache;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraView";
    private CameraHandlerThread thread;

    private FrameCache cache;
    SurfaceHolder holder;
    Camera camera;
    Context context;

    public CameraView(Context context){
        super(context);

        this.context = context;
        this.holder = getHolder();
        this.holder.addCallback(this);
        this.cache = FrameCache.getInstance();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(thread == null)
            thread = new CameraHandlerThread();

        synchronized (thread) {
            thread.openCamera();
        }
    }

    long beingTime = SystemClock.elapsedRealtime();
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initPreview();

        //TODO - optimized buffer somehow
        camera.addCallbackBuffer(previewBuffer());

        camera.setPreviewCallback(new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(final byte[] frame, Camera camera) {

                final int calculated_fps = (int) (1 / ((double) (SystemClock.elapsedRealtime() - beingTime) / 1000));
                beingTime = SystemClock.elapsedRealtime();

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setFPSTextView(calculated_fps);
                    }
                });

                final Camera.Parameters params = camera.getParameters();
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setResolutionTextView(params.getPreviewSize());
                    }
                });

                new Thread(new Runnable() {
                    public void run() {
                        cache.addFrame(frame);
                    }
                }).start();
                camera.addCallbackBuffer(previewBuffer());
            }

        });

        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Set callback to null to prevent it being fired after camera has been released
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
    }

    /**
     * Set text of fps textview in activity_main.xml
     */
    private void setFPSTextView(int fps) {
        ((TextView) ((Activity)context).findViewById(R.id.fps_range)).setText("FPS " + fps);
    }

    private void setResolutionTextView(Camera.Size resolution) {
        ((TextView) ((Activity)context).findViewById(R.id.resolution)).setText("Resolution: " + resolution.width + "-" + resolution.height);
    }

    /**
     * Initialize preview fps range and resolution
     */
    private void initPreview() {
        if (camera!=null){
            Camera.Parameters parameters=camera.getParameters();
            List<int[]> fps = parameters.getSupportedPreviewFpsRange();

            //Set highest max/min fps for camera
            //Best fps range is last item in list
            int min = fps.get(fps.size()-1)[0];
            int max = fps.get(fps.size()-1)[1];
            parameters.setPreviewFpsRange(min,max);

            //Set largest picture size
            List<Camera.Size> e = parameters.getSupportedPreviewSizes();
            //Resolution in list is decreasing (ie, index 0 has best resolution
            Camera.Size sizePref = e.get(0);
            parameters.setPreviewSize(sizePref.width, sizePref.height);

            parameters.setRecordingHint(true);
            camera.setParameters(parameters);
        }
    }

    public  void setCameraOrientation(int cameraId) {
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


    //Create buffer for preview callback
    private byte[] previewBuffer(){
        int bufferSize;
        byte buffer[];
        int bitsPerPixel;

        Camera.Parameters mParams= camera.getParameters();
        Camera.Size mSize= mParams.getPreviewSize();

        int mImageFormat= mParams.getPreviewFormat();

        if(mImageFormat== ImageFormat.NV21){
            int yStride   = (int) Math.ceil(mSize.width / 16.0) * 16;
            int uvStride  = (int) Math.ceil( (yStride / 2) / 16.0) * 16;
            int ySize     = yStride * mSize.height;
            int uvSize    = uvStride * mSize.height / 2;
            bufferSize      = ySize + uvSize * 2;
            buffer=new byte[bufferSize];

            return buffer;
        }

        bitsPerPixel=ImageFormat.getBitsPerPixel(mImageFormat);
        bufferSize= (int)(mSize.height*mSize.width*((bitsPerPixel/(float)8)));
        buffer=new byte[bufferSize];
        return buffer;
    }

    /**
     * TODO - remove this and fix scoping issue
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

        public void openCamera() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                        try{
                            camera.setPreviewDisplay(holder);
                            setCameraOrientation(Camera.CameraInfo.CAMERA_FACING_BACK);
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
    }
}
