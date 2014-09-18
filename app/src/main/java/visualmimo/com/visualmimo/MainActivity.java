package visualmimo.com.visualmimo;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


public class MainActivity extends Activity {

    private SurfaceHolder previewHolder=null;
    private Camera camera=null;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SurfaceView preview=(SurfaceView)findViewById(R.id.preview);
        previewHolder=preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
    }

    @Override
    public void onResume() {
        super.onResume();

        camera=Camera.open();
        startPreview();
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

        camera.release();
        camera=null;
        inPreview=false;

        super.onPause();
    }

    public void launchVideo(View view) {
        Toast.makeText(MainActivity.this, "Not set yet." , Toast.LENGTH_LONG)
                .show();
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    private void initPreview(int width, int height) {
        if (camera!=null && previewHolder.getSurface()!=null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Log.e("**",
                        "Exception in setPreviewDisplay()", t);
            }

            if (!cameraConfigured) {
                // Set Camera resolution & fps
                Camera.Parameters parameters=camera.getParameters();
                List<int[]> fps = parameters.getSupportedPreviewFpsRange();

                //Set highest max/min fps
                parameters.setPreviewFpsRange(fps.get(fps.size()-1)[0],fps.get(fps.size()-1)[1]);

                List<Camera.Size> e = parameters.getSupportedPictureSizes();

                //Get largest resolution
                Camera.Size sizePref = e.get(0);
                parameters.setPictureSize(sizePref.width, sizePref.height);

                Camera.Size size=getBestPreviewSize(width, height,
                        parameters);

                if (size!=null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured=true;
                }
            }
        }
    }

    private void startPreview() {
        if (cameraConfigured && camera!=null) {
            camera.startPreview();
            inPreview=true;
        }
    }


    SurfaceHolder.Callback surfaceCallback= new SurfaceHolder.Callback() {

        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
            // preview.
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.d("??", "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            initPreview(width, height);

            camera.setPreviewCallback(new Camera.PreviewCallback() {

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    // Do stuff with 'data' which holds frame info.
                }

            });

            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
            //camera.stopPreview();
        }
    };
}
