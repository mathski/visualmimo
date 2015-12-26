/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia.samples.SampleApplication;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.android.visualmimo.VuforiaActivity;
import com.android.visualmimo.camera.ImageTargetRenderer;
import com.android.visualmimo.persistence.MIMOFrame;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.Vuforia.UpdateCallbackInterface;
import com.qualcomm.vuforia.samples.SampleApplication.utils.VuforiaGLView;

import java.util.ArrayList;

/**
 * This file is straight from the Vuforia samples, handles stuff behind the scenes.
 *
 * As of December 21, this is being reworked to act more like a modular singleton.
 * The original implementation made many references to MainActivity and depended on methods from that specific activity.
 * Instead, it should theoretically work on any activity.
 * This will reduce the amount of code needed to make an activity work, which is ideal for future implementations.
 *
 * This class functions as a Singleton. Use VuforiaSession.getInstance(this) to get a copy of the instance (you must call this from an Activity which extends VuforiaActivity).
 * For more information on implementation, view the VuforiaActivity.java file.
 */
public class VuforiaSession implements UpdateCallbackInterface{
    
    private static final String LOGTAG = "VMIMO";
    
    // References to the current activity
    private VuforiaActivity mActivity;
    private VuforiaActivity mSessionControl;
    
    // Display size of the device:
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    
    // The async tasks to initialize the Vuforia SDK:
    private InitVuforiaTask mInitVuforiaTask;
    private LoadTrackerTask mLoadTrackerTask;
    
    // Used for Asynch loading as a reference in the event of lifecycle changes.
    private Object mShutdownLock = new Object();
    
    // Vuforia initialization flags:
    private int mVuforiaFlags = 0;
    
    // Holds the camera configuration to use upon resuming
    private int mCamera = CameraDevice.CAMERA.CAMERA_DEFAULT;
    private boolean mStarted = false;
    private boolean mCameraRunning = false;
    
    // Stores the projection matrix to use for rendering purposes
    private Matrix44F mProjectionMatrix;
    
    // Stores orientation
    private boolean mIsPortrait = false;

    public VuforiaGLView mGlView;
    public ImageTargetRenderer mRenderer;

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    private ArrayList<VuforiaActivity> activities = new ArrayList<VuforiaActivity>();

    //Instance variables for this class to act as a Singleton
    public static VuforiaSession _INSTANCE = null;
    public static boolean _IR_INITIALIZED = false;

    /**
     * Gets an instance of the VuforiaSession.
     * @param activity An activity which will be used as the base activity if an instance doesn't already exist.
     * @return VuforiaSession an instance of VuforiaSession, creating a new one if it didn't already exist.
     */
    public static VuforiaSession getInstance(VuforiaActivity activity){
        if(_INSTANCE != null) return _INSTANCE;
        return ( _INSTANCE = new VuforiaSession(activity));
    }

    private VuforiaSession(VuforiaActivity sessionControl){
        mSessionControl = sessionControl;
        mDatasetStrings.add("VMIMO.xml");
        activities.add(sessionControl);
    }

    /**
     * Adds an activity to the activity list of the session. Only way to have an activity's onQCARUpdate(State) method updated.
     * @param activity The Activity to add to the activities list.
     */
    public void addSelfToActivities(VuforiaActivity activity){
       if(!activities.contains(activity)) activities.add(activity);
    }

    /**
     * Initializes Vuforia AR and enables preferences.
     *
     * @param activity Activity to use to access context details.
     * @param screenOrientation Screen orientation that the app will run in (portrait is recommended).
     */
    public void initAR(VuforiaActivity activity, int screenOrientation)
    {
        if(_IR_INITIALIZED){
            mActivity = mSessionControl = activity;
            mActivity.setRequestedOrientation(screenOrientation);
            return;
        }
        _IR_INITIALIZED = true;
        VuforiaException vuforiaException = null;
        mActivity = activity;
        
        if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        
        mActivity.setRequestedOrientation(screenOrientation);
        updateActivityOrientation();
        storeScreenDimensions();
        
        // As long as this window is visible to the user, keep the device's screen turned on and bright:
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mVuforiaFlags = Vuforia.GL_20;
        
        // Initialize Vuforia SDK asynchronously to avoid blocking the main (UI) thread.
        if (mInitVuforiaTask != null) {
            String logMessage = "Vuforia Already Initialized";
            vuforiaException = new VuforiaException(VuforiaException.VUFORIA_ALREADY_INITIALIZATED, logMessage);
            Log.e(LOGTAG, logMessage);
        }
        
        if (vuforiaException == null){
            try{
                mInitVuforiaTask = new InitVuforiaTask();
                mInitVuforiaTask.execute();
            } catch (Exception e){
                String logMessage = "Initializing Vuforia SDK failed";
                vuforiaException = new VuforiaException(
                    VuforiaException.INITIALIZATION_FAILURE,
                    logMessage);
                Log.e(LOGTAG, logMessage);
            }
        }
        
        if (vuforiaException != null) onInitARDone(vuforiaException);
    }

    /**
     * Creates the GL views and renderer
     * Called by #onInitARDone(VuforiaException)
     */
    private void initApplicationAR() {
       /* int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new VuforiaGLView(mActivity);
        mGlView.init(translucent, depthSize, stencilSize);
*/
        mRenderer = new ImageTargetRenderer(mActivity, this);
        //mGlView.setRenderer(mRenderer);

    }

    /**
     * Adds the GLView to the activity's layout.
     * Used to display the AR camera window on an activity view.
     * @param activity Activity to draw onto.
     * @param mUILayout Layout to draw onto.
     */
    public void drawOnView(Activity activity, RelativeLayout mUILayout){
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        mGlView = new VuforiaGLView(activity);
        mGlView.init(translucent, depthSize, stencilSize);
        mGlView.setRenderer(mRenderer);

        activity.addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mUILayout.bringToFront();
        mUILayout.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Handles starting the AR if no errors were thrown in AR initialization.
     * Called by #initAR(VuforiaActivity, int)
     * @param exception Exception thrown during AR initialization.
     */
    public void onInitARDone(VuforiaException exception) {
        if (exception == null) {
            initApplicationAR();

            mRenderer.mIsActive = true;

            drawOnView(mActivity, mActivity.mUILayout);

            try {
                startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (VuforiaException e) {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result) Log.e(LOGTAG, "Unable to enable continuous autofocus");
        } else {
            Log.e(LOGTAG, exception.getString());
            mSessionControl.finish();
        }
    }

    /**
     * A wrapper method to start AR in the event that an activity doesn't know the camera ID.
     * Calls #startCamera(int) directly with the saved camera value, or returns if one was never set (can't be called before #startAR(int) ).
     * @throws VuforiaException
     */
    public void startAR() throws VuforiaException {
        if(mCamera == 0) return;
        startAR(mCamera);
    }

    /**
     * Starts Vuforia and loads the camera + trackers.
     * To be called in the onCreate method on any activity implementing AR.
     * @param camera Camera ID to use.
     * @throws VuforiaException
     */
    public void startAR(int camera) throws VuforiaException{
        String error;
        if(mCameraRunning) {
        	error = "Camera already running, unable to open again";
        	Log.e(LOGTAG, error);
            throw new VuforiaException(
                VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        mCamera = camera;
        if (!CameraDevice.getInstance().init(camera)) {
            error = "Unable to open camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new VuforiaException(
                VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        configureVideoBackground();
        
        if (!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT)) {
            error = "Unable to set video mode";
            Log.e(LOGTAG, error);
            throw new VuforiaException(
                VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new VuforiaException(
                VuforiaException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        Vuforia.setFrameFormat(MIMOFrame.IMAGE_FORMAT, true);
        
        setProjectionMatrix();
        
        doStartTrackers();
        
        mCameraRunning = true;
        
        try {
            setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
        } catch (VuforiaException exceptionTriggerAuto) {
            setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }
    }

    /**
     * Loads tracker data from VMIMO.xml
     * @return Returns true if trackers loaded successfully, or false if not.
     */
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker imageTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (imageTracker == null) return false;

        if (mCurrentDataset == null) mCurrentDataset = imageTracker.createDataSet();

        if (mCurrentDataset == null) return false;

        if (!mCurrentDataset.load(mDatasetStrings.get(mCurrentDatasetSelectionIndex), STORAGE_TYPE.STORAGE_APPRESOURCE)) return false;

        if (!imageTracker.activateDataSet(mCurrentDataset)) return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = mCurrentDataset.getTrackable(count);

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data " + (String) trackable.getUserData());
        }

        return true;
    }

    /**
     * Unloads trackers data when AR is being stopped.
     * @return Returns true if trackers unloaded succesfully, or false if not.
     */
    public boolean doUnloadTrackersData() {
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker imageTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
        if (imageTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive()) {
            if (imageTracker.getActiveDataSet().equals(mCurrentDataset) && !imageTracker.deactivateDataSet(mCurrentDataset)) {
                result = false;
            } else if (!imageTracker.destroyDataSet(mCurrentDataset)) {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }

    /**
     * Resets trackers data to their default values.
     * @return Returns true if values reset successfully, or false otherwise.
     */
    public boolean doDeinitTrackers() {
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    /**
     * Stops Vuforia and unloads everything -- trackers, camera, etc.
     * @throws VuforiaException
     */
    public void stopAR() throws VuforiaException
    {
        // Cancel potentially running tasks
        if (mInitVuforiaTask != null && mInitVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED){
            mInitVuforiaTask.cancel(true);
            mInitVuforiaTask = null;
        }
        
        if (mLoadTrackerTask != null && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED){
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }
        
        mInitVuforiaTask = null;
        mLoadTrackerTask = null;
        
        mStarted = false;
        
        stopCamera();
        
        // Ensure that all asynchronous operations to initialize Vuforia and loading the tracker datasets do not overlap:
        synchronized (mShutdownLock) {
            
            boolean unloadTrackersResult = doUnloadTrackersData();
            boolean deinitTrackersResult = doDeinitTrackers();
            Vuforia.deinit();
            
            if (!unloadTrackersResult)
                throw new VuforiaException(VuforiaException.UNLOADING_TRACKERS_FAILURE, "Failed to unload trackers\' data");
            
            if (!deinitTrackersResult)
                throw new VuforiaException(VuforiaException.TRACKERS_DEINITIALIZATION_FAILURE, "Failed to deinitialize trackers");
            
        }
    }

    /**
     * Resumes Vuforia and trackers.
     * Calls #startAR(int)
     * @throws VuforiaException
     */
    public void resumeAR() throws VuforiaException
    {
        Vuforia.onResume();
        
        if (mStarted) startAR(mCamera);
    }


    /**
     * Stops Vuforia & the camera.
     * @throws VuforiaException
     */
    public void pauseAR() throws VuforiaException
    {
        if (mStarted) stopCamera();

        Vuforia.onPause();
    }


    /**
     * Getter for the projection matrix.
     * @return A Projection Matrix to be used within the application.
     */
    public Matrix44F getProjectionMatrix()
    {
        return mProjectionMatrix;
    }

    /**
     * Loops through all linked activites ( #addSelfToActivities(VuforiaActivity) ) and calls their onQCarUpdate(State) method.
     * @param state State to be passed to #VuforiaActivity.onQCarUpdate(State)
     */
    public void QCAR_onUpdate(State state){
        //for(VuforiaActivity activity : activities) activity.onQCARUpdate(state);
        mActivity.onQCARUpdate(state);
    }

    /**
     * To be called whenever Vuforia settings are updated, or the display changes (orientation, size, etc).
     * Resets the activity's orientation, dimensions, background, and projection matrix.
     */
    public void onConfigurationChanged()
    {
        updateActivityOrientation();
        storeScreenDimensions();
        
        if (isARRunning()) {
            configureVideoBackground();
            setProjectionMatrix();
        }
        
    }

    /**
     * Resumes Vuforia if paused.
     */
    public void onResume()
    {
        Vuforia.onResume();
    }

    /**
     * Pauses Vuforia if running.
     */
    public void onPause()
    {
        Vuforia.onPause();
    }
    
    
    public void onSurfaceChanged(int width, int height)
    {
        Vuforia.onSurfaceChanged(width, height);
    }
    
    
    public void onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated();
    }

    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean>
    {

        /**
         * An Asynchronous task which handles Vuforia's
         */

        private int mProgressValue = -1;
        private static final String _LICENSE_KEY = "AdbD213/////AAAAAUZeX2UcFUEtipsr8m0qIBM1Ap4yx6SzVqV4UqoBzJ5iSboEW5dbY/3uH2ffUlfF4DzM10jLbQXx7ndgMToiK+VaSyGGdoaMmvkPyUvSf76+wV8C0gNzw74JpcpWkzS/If3pr8z84yo6CZqf5Rw5052BNUqkIw7hkhiRV2ZFmvZTAnGly7h38TzWaqPp2SqlJP5Ebbg6faLzspoLW6UcH3A6o2XUeSKvudBvrdhvWcQrKf+J1ZQq5Zv8BTQgszbavRtLetnPKulrPY+arhFNHlpeisjYi3Vt2nm4dX1kMjjST09guIiqfIXHR14+q7nJBBtWUDzGuJm1KDEqewbUq5RataKFvzfphWJYdG+18Xvq";

        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized (mShutdownLock) {
                Vuforia.setInitParameters(mActivity, mVuforiaFlags, _LICENSE_KEY);

                do {
                    //Continually updates progess by reporting percentage complete as init continues. If progress == -1, something went wrong.
                    mProgressValue = Vuforia.init();
                    
                    publishProgress(mProgressValue);

                    //Continue running async task until either complete or a stop requested.
                } while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);
                
                return (mProgressValue > 0);
            }
        }

        /**
         * Evaluates all components loaded correctly and throws error messages if any failed.
         * Called after initialization completed.
         * @param result Whether or not init was successful.
         */
        protected void onPostExecute(Boolean result)
        {
            // Done initializing Vuforia, proceed to next application initialization status:
            VuforiaException vuforiaException = null;
            
            if (result){
                Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia " + "initialization successful");
                
                boolean initTrackersResult;
                initTrackersResult = doInitTrackers();
                
                if (initTrackersResult) {
                    try {
                        mLoadTrackerTask = new LoadTrackerTask();
                        mLoadTrackerTask.execute();
                    } catch (Exception e) {
                        String logMessage = "Loading tracking data set failed";
                        vuforiaException = new VuforiaException(VuforiaException.LOADING_TRACKERS_FAILURE, logMessage);
                        Log.e(LOGTAG, logMessage);
                        onInitARDone(vuforiaException);
                    }
                    
                } else {
                    vuforiaException = new VuforiaException(
                        VuforiaException.TRACKERS_INITIALIZATION_FAILURE,
                        "Failed to initialize trackers");
                    onInitARDone(vuforiaException);
                }
            } else {
                String logMessage;
                
                // NOTE: Check if initialization failed because the device is not supported. At this point the user should be informed with a message.
                if (mProgressValue == Vuforia.INIT_DEVICE_NOT_SUPPORTED) {
                    logMessage = "Failed to initialize Vuforia because this " + "device is not supported.";
                } else {
                    logMessage = "Failed to initialize Vuforia.";
                }
                
                // Log error:
                Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + logMessage + " Exiting.");
                
                // Send Vuforia Exception to the application and call initDone to stop initialization process
                vuforiaException = new VuforiaException(
                    VuforiaException.INITIALIZATION_FAILURE,
                    logMessage);
                onInitARDone(vuforiaException);
            }
        }
    }

    /**
     * Initializes the trackers.
     * @return Returns true if trackers successfully initialized, false otherwise.
     */
    public boolean doInitTrackers() {
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }

    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean> {
        /**
         * AsyncTask to load Trackers in in a separate thread.
         */

        protected Boolean doInBackground(Void... params) {
            synchronized (mShutdownLock) {
                return doLoadTrackersData();
            }
        }
        

        protected void onPostExecute(Boolean result)
        {
            Log.d(LOGTAG, "ONPOSTEXECUTE CALLED");
            VuforiaException vuforiaException = null;
            
            Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution " + (result ? "successful" : "failed"));
            
            if (!result) {
                String logMessage = "Failed to load tracker data.";
                // Error loading dataset
                Log.e(LOGTAG, logMessage);
                vuforiaException = new VuforiaException(
                    VuforiaException.LOADING_TRACKERS_FAILURE,
                    logMessage);
            } else {
                System.gc();
                
                Vuforia.registerCallback(VuforiaSession.this);
                
                mStarted = true;
            }
            
            // Done loading the tracker, update application status, send the exception to check errors
            onInitARDone(vuforiaException);
        }
    }

    /**
     * Updates instance variables with current screen dimensions.
     */
    private void storeScreenDimensions() {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    /**
     * Sets  the current orientation based on the orientation of the activity.
     */
    private void updateActivityOrientation() {
        Configuration config = mActivity.getResources().getConfiguration();
        
        switch (config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mIsPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mIsPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }
        
        Log.i(LOGTAG, "Activity is in " + (mIsPortrait ? "PORTRAIT" : "LANDSCAPE"));
    }

    /**
     * Updates the projection matrix used for AR rendering.
     */
    private void setProjectionMatrix()
    {
        CameraCalibration camCal = CameraDevice.getInstance().getCameraCalibration();
        mProjectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 5000.0f);
    }

    /**
     * Starts the trackers.
     * @return Returns true if trackers started successfully, false otherwise.
     */
    public boolean doStartTrackers() {
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (imageTracker != null) imageTracker.start();

        return result;
    }

    /**
     * Stops the trackers.
     * @return Returns true if trackers stopped successfully, false otherwise.
     */
    public boolean doStopTrackers() {
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (imageTracker != null) imageTracker.stop();

        return result;
    }

    /**
     * Stops the camera. Also stops trackers.
     */
    public void stopCamera(){
        if(mCameraRunning){
            doStopTrackers();
            CameraDevice.getInstance().stop();
            CameraDevice.getInstance().deinit();
            mCameraRunning = false;
        }
    }

    /**
     * Sets the focus of the camera or throws an error.
     * @param mode Display mode to set the camera to.
     * @return Returns true if successfully set focus, false otherwise.
     * @throws VuforiaException
     */
    private boolean setFocusMode(int mode) throws VuforiaException{
        boolean result = CameraDevice.getInstance().setFocusMode(mode);
        
        if (!result) throw new VuforiaException(VuforiaException.SET_FOCUS_MODE_FAILURE, "Failed to set focus mode: " + mode);
        
        return result;
    }

    /**
     * Configures the video mode and sets offsets for the camera's image
     */
    private void configureVideoBackground(){
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        
        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setPosition(new Vec2I(0, 0));
        
        int xSize = 0, ySize = 0;
        if (mIsPortrait){
            xSize = (int) (vm.getHeight() * (mScreenHeight / (float) vm.getWidth()));
            ySize = mScreenHeight;
            
            if (xSize < mScreenWidth)
            {
                xSize = mScreenWidth;
                ySize = (int) (mScreenWidth * (vm.getWidth() / (float) vm.getHeight()));
            }
        } else{
            xSize = mScreenWidth;
            ySize = (int) (vm.getHeight() * (mScreenWidth / (float) vm.getWidth()));
            
            if (ySize < mScreenHeight)
            {
                xSize = (int) (mScreenHeight * (vm.getWidth() / (float) vm.getHeight()));
                ySize = mScreenHeight;
            }
        }
        
        config.setSize(new Vec2I(xSize, ySize));
        
        Log.i(LOGTAG, "Configure Video Background : Video (" + vm.getWidth()
            + " , " + vm.getHeight() + "), Screen (" + mScreenWidth + " , "
            + mScreenHeight + "), mSize (" + xSize + " , " + ySize + ")");
        
        Renderer.getInstance().setVideoBackgroundConfig(config);
        
    }

    /**
     * Determines if AR, trackers, and camera are all loaded and running successfully.
     * @return Returns true if AR is running, false if not.
     */
    private boolean isARRunning()
    {
        return mStarted;
    }
    
}
