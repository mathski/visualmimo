package com.android.visualmimo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.visualmimo.persistence.FrameCache;
import com.android.visualmimo.persistence.MIMOFrame;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.Image;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.samples.SampleApplication.VuforiaException;
import com.qualcomm.vuforia.samples.SampleApplication.VuforiaSession;

import java.nio.ByteBuffer;

/**
 * Created by joeb3219 on 12/21/2015.
 */
public abstract class VuforiaActivity extends Activity implements Handler.Callback {

    protected static final String LOGTAG = "ImageTargets";
    public RelativeLayout mUILayout;
    /** The number of images to save when we are recording. */
    protected int NUM_SAVES = 1;
    protected int saveCount = 0;
    protected int frameCount = 0;

    protected RequestQueue queue;
    /** FrameCache to which we add Frames as they come. */
    protected FrameCache cache;

    protected VuforiaSession vuforiaAppSession;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cache = FrameCache.getInstance();
        vuforiaAppSession = VuforiaSession.getInstance(this);
        queue = Volley.newRequestQueue(this);
        vuforiaAppSession.addSelfToActivities(this);
    }

    /**
     * The method called to update QCAR by VuforiaSession.
     * Every activity which is part of the session will have their RESPECTIVE #onQCARUpdate(State) method called.
     * A default implementation is found in #onQCARUpdate(State, boolean) -- this is generally called from onQCARUpdate(State) in each activity, passing whether or not to take a picture.
     * @param state State of QCAR.
     */
    public void onQCARUpdate(State state) {onQCARUpdate(state, false);}

    /**
     * A default implementation of onQCARUpdate.
     * @param state STate of QCAR.
     * @param takePicture Whether or not to take a picture
     */
    protected void onQCARUpdate(State state, boolean takePicture){
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

            // extract corners of trackable
            // adapted from
            // https://developer.vuforia.com/forum/android/get-trackable-angle
            ImageTarget imageTarget = (ImageTarget) trackable;
            Vec3F targetSize = imageTarget.getSize();

            float halfWidth = targetSize.getData()[0] / 2.0f;
            float halfHeight = targetSize.getData()[1] / 2.0f;

            CameraCalibration cameraCalibration = CameraDevice.getInstance()
                    .getCameraCalibration();

            Vec2F v1 = Tool.projectPoint(cameraCalibration, result.getPose(),
                    new Vec3F(-halfWidth, halfHeight, 0));
            Vec2F v2 = Tool.projectPoint(cameraCalibration, result.getPose(),
                    new Vec3F(halfWidth, halfHeight, 0));
            Vec2F v3 = Tool.projectPoint(cameraCalibration, result.getPose(),
                    new Vec3F(halfWidth, -halfHeight, 0));
            Vec2F v4 = Tool.projectPoint(cameraCalibration, result.getPose(),
                    new Vec3F(-halfWidth, -halfHeight, 0));

            final float[][] corners = new float[4][];
            corners[0] = v1.getData();
            corners[1] = v2.getData();
            corners[2] = v3.getData();
            corners[3] = v4.getData();

            Frame frame = state.getFrame();
            Image image = null;

            for (int i = 0; i < frame.getNumImages(); i++) {
                Image temp = frame.getImage(i);
                if (temp.getFormat() == MIMOFrame.IMAGE_FORMAT) {
                    image = temp;
                    break;
                }
            }

            if (image == null) {
                System.out.println("Unable to get image in RGB888.");
                return;
            }

            ByteBuffer pixels = image.getPixels();
            final byte[] pixelArray = new byte[pixels.remaining()];
            pixels.get(pixelArray, 0, pixelArray.length);
            final int imageWidth = image.getWidth();
            final int imageHeight = image.getHeight();
            final int stride = image.getStride();

            // Add frame to FrameCache.
//            new Thread(new Runnable() {
//                public void run() {
                    cache.addFrame(pixelArray, imageWidth, imageHeight, corners);
//                }
//            }).start();

            if (takePicture) {
                saveCount++;
                if (saveCount <= NUM_SAVES) {
                    FrameProcessing.processFrames(
                            cache,
                            this,
                            false,
                            imageWidth,
                            imageHeight);
                } else {
//                    saveCount = 0;
                }
            }

        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Generic onResume() method which resumes AR & GL Views.
     */
    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        try {
            vuforiaAppSession.resumeAR();
        } catch (VuforiaException e) {
            Log.e(LOGTAG, e.getString());
        }

        if (vuforiaAppSession.mGlView != null) {
            vuforiaAppSession.mGlView.setVisibility(View.VISIBLE);
            vuforiaAppSession.mGlView.onResume();
        }
    }

    /**
     * Generic onPause() method which will pause AR & GL Views
     */
    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (vuforiaAppSession.mGlView != null) {
            vuforiaAppSession.mGlView.setVisibility(View.INVISIBLE);
            vuforiaAppSession.mGlView.onPause();
        }

        try {
            vuforiaAppSession.pauseAR();
        } catch (VuforiaException e) {
            Log.e(LOGTAG, e.getString());
        }
    }

    /**
     * Generic onDestroy() method which will stop the AR.
     */
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try {
            vuforiaAppSession.stopAR();
            if(mUILayout != null){
                ((RelativeLayout) findViewById(R.id.layout_wrapper)).removeAllViews();
            }
        } catch (VuforiaException e) {
            Log.e(LOGTAG, e.getString());
        }

        System.gc();
    }

}
