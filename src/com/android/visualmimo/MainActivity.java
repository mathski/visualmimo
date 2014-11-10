/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.android.visualmimo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.qualcomm.QCAR.QCAR;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.Image;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationException;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.android.visualmimo.R;
import com.android.visualmimo.camera.CameraView;
import com.android.visualmimo.camera.DrawView;
import com.android.visualmimo.camera.ImageProcessing;
import com.android.visualmimo.camera.ImageTargetRenderer;
import com.android.visualmimo.persistence.FrameCache;
import com.android.visualmimo.persistence.MIMOFrame;

/**
 * Activity that handles everything: Vuforia, UI.
 * @author alexio, revan
 *
 */
public class MainActivity extends Activity
{
	/** The number of images to save when we are recording.*/
    private static final int NUM_SAVES = 50;
    private int saveCount = 0;
    private boolean recordingMode = false;
    private static final String savePath = "/sdcard/vmimo/";
    private String saveDir;
    
    private static final String LOGTAG = "ImageTargets";
    
    SampleApplicationSession vuforiaAppSession;
    
    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();
    
    // Our OpenGL view:
    private SampleApplicationGLView mGlView;
    
    // Our renderer:
    private ImageTargetRenderer mRenderer;
    
    private GestureDetector mGestureDetector;
    
    private boolean mSwitchDatasetAsap = false;
    private boolean mFlash = false;
    private boolean mContAutofocus = false;
    
    private View mFlashOptionView;
    
    private RelativeLayout mUILayout;
    
    public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    
    private FrameLayout layout;
    private DrawView drawView;
//    private CameraView cameraView;
    private int originalHeight;
    private int originalWidth;
    private Button switchButton;
    ImageProcessing processor;
    
    /** FrameCache to which we add Frames as they come. */
    private FrameCache cache;
    
    private int viewStatus = 0;
    
    static {
	    System.loadLibrary("ndk1");
	}
    
    
    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        layout = (FrameLayout) findViewById(R.id.cameraView);
        switchButton = (Button) findViewById(R.id.switch_button);
        
        //NOTE(revan): Vuforia handles the video output on its own, so I don't
        //             think we need the cameraView any more. 
//        cameraView = new CameraView(this);
        drawView = new DrawView(this);

        layout.addView(drawView);
//        layout.addView(cameraView);
        layout.bringChildToFront(drawView);
        processor = new ImageProcessing();
        processor.setContext(this);
        drawView.setProcessor(processor);
//        cameraView.setImageProcessor(processor);
        
        cache = FrameCache.getInstance();
        
        vuforiaAppSession = new SampleApplicationSession(this);
        
        startLoadingAnimation();
        mDatasetStrings.add("StonesAndChips.xml");
        mDatasetStrings.add("Tarmac.xml");
        
        vuforiaAppSession
            .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        mGestureDetector = new GestureDetector(this, new GestureListener());
    }
    
    /** GestureListener for tap to focus. */
    private class GestureListener extends
        GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();
        
        
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }
        
        
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                    
                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);
            
            return true;
        }
    }
    
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        
        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
        
//        getViewSize();
        viewStatus = 0;
        switchView(null);
    }
    
    /**This helps avoid the app dying when settings change.*/
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        vuforiaAppSession.onConfigurationChanged();
    }
    
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }
        
        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        processor.stopProcessing();
    }
    
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        
        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        System.gc();
    }
    
    /** Adds ActionBar items. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    /**Initializes AR application components.*/
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        mRenderer = new ImageTargetRenderer(this, vuforiaAppSession);
        mGlView.setRenderer(mRenderer);
        
    }
    
    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.activity_main,
            null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
//        mUILayout.setBackgroundColor(Color.BLACK);
        mUILayout.setBackgroundColor(Color.TRANSPARENT);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
    }
    
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
            .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
            return false;
        
        if (mCurrentDataset == null)
            mCurrentDataset = imageTracker.createDataSet();
        
        if (mCurrentDataset == null)
            return false;
        
        if (!mCurrentDataset.load(
            mDatasetStrings.get(mCurrentDatasetSelectionIndex),
            STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;
        
        if (!imageTracker.activateDataSet(mCurrentDataset))
            return false;
        
        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            
            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                + (String) trackable.getUserData());
        }
        
        return true;
    }
    
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
            .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
            return false;
        
        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (imageTracker.getActiveDataSet().equals(mCurrentDataset)
                && !imageTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!imageTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }
            
            mCurrentDataset = null;
        }
        
        return result;
    }
    
    public void onInitARDone(SampleApplicationException exception)
    {
        
        if (exception == null)
        {
            initApplicationAR();
            
            mRenderer.mIsActive = true;
            
            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
            
            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            
            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
            
            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }
            
            boolean result = CameraDevice.getInstance().setFocusMode(
                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
            
            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");
        } else
        {
            Log.e(LOGTAG, exception.getString());
            finish();
        }
    }
    
    /** Create new MIMOFrame, add to FrameCache. */
    public void onQCARUpdate(State state)
    {
    	Frame frame = state.getFrame();
    	Image image = null;
    	
    	//NOTE(revan): Vuforia is weird and gives back a whole bunch of formats.
    	//Loop to get right one.
    	for (int i = 0; i < frame.getNumImages(); i++) {
    		System.out.println("Image " + i);
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
        
        //NOTE(revan): debug prints
        System.out.println("Image width: " + imageWidth);
        System.out.println("Image height: " + imageHeight);
        System.out.println("Image stride: " + stride);
        System.out.println("First pixel byte: " + pixelArray[0]);
        
        //Add frame to FrameCache.
        new Thread(new Runnable() {
            public void run() {
                cache.addFrame(pixelArray, imageWidth, imageHeight);
            }
        }).start();
                
        if(recordingMode) {
        	saveCount++;
        	if (saveCount < NUM_SAVES) {
		        //Queue file write.
        		new Thread(new Runnable() {
        			public void run() {
						String filePath = savePath + saveDir + "/" + saveCount + ".rgb888";
						File file = new File(filePath);
						try {
							file.delete();
							file.createNewFile();
							FileOutputStream stream = new FileOutputStream(file);
							stream.write(pixelArray);
						} catch (IOException e) {
							e.printStackTrace();
						}
        			}
        		}).start();
        	} else {
        		recordingMode = false;
        		showToast("Done saving burst.");
        	}
        }
    	
        if (mSwitchDatasetAsap)
        {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ImageTracker it = (ImageTracker) tm.getTracker(ImageTracker
                .getClassType());
            if (it == null || mCurrentDataset == null
                || it.getActiveDataSet() == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }
            
            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }
    
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;
        
        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ImageTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }
    
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.start();
        
        return result;
    }
    
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();
        
        return result;
    }
    
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ImageTracker.getClassType());
        
        return result;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return mGestureDetector.onTouchEvent(event);
    }
    
    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_AUTOFOCUS = 2;
    final public static int CMD_FLASH = 3;
    final public static int CMD_CAMERA_FRONT = 4;
    final public static int CMD_CAMERA_REAR = 5;
    final public static int CMD_DATASET_START_INDEX = 6;
    
//    public void getViewSize() {
//        originalHeight = cameraView.getLayoutParams().height;
//        originalWidth = cameraView.getLayoutParams().width;
//    }

    public void drawViewSize(int width, int height) {
        drawView.getLayoutParams().height = height;
        drawView.getLayoutParams().width = width;
        drawView.requestLayout();
    }

//    public void cameraViewSize(int width, int height) {
//        cameraView.getLayoutParams().height = height;
//        cameraView.getLayoutParams().width = width;
//        cameraView.requestLayout();
//    }
    
    public void displayCamera() {
//    	cameraViewSize(originalWidth, originalHeight);
    	//NOTE(revan): I think we can set visibility instead of resizing, right?
    	mGlView.setVisibility(View.VISIBLE);
        drawViewSize(0,0);
        switchButton.setText("Camera");
    }
    
    public void displayDrawView() {
//    	cameraViewSize(0, 0);
    	mGlView.setVisibility(View.INVISIBLE);
        drawViewSize(originalWidth, originalHeight);
        switchButton.setText(drawView.getProcessingType());
    }

    /**
     * Called when button is pressed, cycles between video types.
     * NOTE(revan): this may be broken.
     */ 
    public void switchView(View view) {
        switch(viewStatus) {
            case 0: //Camera
            	Log.e("&", "& Camera");
                displayCamera();
                viewStatus++;
                showToast("Camera");
                break;
            case 1:
            	drawView.setProcessingType(DrawView.ProcessingType.Subtraction);
            	Log.e("&", "&" + drawView.getProcessingType());
                displayDrawView();
                viewStatus++;
                showToast("Subtraction");
                break;
            default:
            	
            	drawView.setProcessingType(DrawView.ProcessingType.Division);
            	Log.e("&", "& " + drawView.getProcessingType());
            	displayDrawView();
            	viewStatus = 0;
            	showToast("Division");
            	break;
        }
    }
    
    /** Handles ActionBar presses. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
            	//enable recording mode
                recordingMode = true;
                
                //save to folder of current system time
                saveDir = "" + System.currentTimeMillis();
                new File(savePath + saveDir).mkdirs();
                
                saveCount = 0;
                
                return true;
            case R.id.action_settings:
                //TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
