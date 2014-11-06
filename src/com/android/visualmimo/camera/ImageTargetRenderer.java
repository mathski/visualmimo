/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.android.visualmimo.camera;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.QCAR.QCAR;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.ImageTargetBuilder;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.android.visualmimo.MainActivity;


// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "ImageTargetRenderer";
    
    private SampleApplicationSession vuforiaAppSession;
    private MainActivity mActivity;
    
    private int shaderProgramID;
    
    private Renderer mRenderer;
    
    public boolean mIsActive = false;
    
    private static final float OBJECT_SCALE_FLOAT = 3.0f;
    
    public ImageTargetRenderer(MainActivity activity,
        SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        initRendering();
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        mRenderer = Renderer.getInstance();

        // Hide the Loading Dialog
        mActivity.loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        
    }
    
    
    // The render function.
    private void renderFrame()
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera
            
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            
            int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0
                : 1;
            textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 2
                : textureIndex;
            
            
            //extract corners of trackable
            //adapted from https://developer.vuforia.com/forum/android/get-trackable-angle
            ImageTarget imageTarget = (ImageTarget) trackable;
            Vec2F targetSize = imageTarget.getSize();
            
            float halfWidth = targetSize.getData()[0] / 2.0f;
            float halfHeight = targetSize.getData()[1] / 2.0f;
            
            CameraCalibration cameraCalibration = CameraDevice.getInstance().getCameraCalibration();

            Vec2F v1 = Tool.projectPoint(cameraCalibration, result.getPose(), new Vec3F(-halfWidth, halfHeight, 0));
            Vec2F v2 = Tool.projectPoint(cameraCalibration, result.getPose(), new Vec3F(halfWidth, halfHeight, 0));
            Vec2F v3 = Tool.projectPoint(cameraCalibration, result.getPose(), new Vec3F(halfWidth, -halfHeight, 0));
            Vec2F v4 = Tool.projectPoint(cameraCalibration, result.getPose(), new Vec3F(-halfWidth, -halfHeight, 0));
            
            Log.d(LOGTAG, "Corners:");
            for (int i = 0; i < v1.getData().length; i++) {
            	System.out.print(v1.getData()[i] + " ");
            }
            System.out.println();
            for (int i = 0; i < v2.getData().length; i++) {
            	System.out.print(v2.getData()[i] + " ");
            }
            System.out.println();
            for (int i = 0; i < v3.getData().length; i++) {
            	System.out.print(v3.getData()[i] + " ");
            }
            System.out.println();
            for (int i = 0; i < v4.getData().length; i++) {
            	System.out.print(v4.getData()[i] + " ");
            }
            System.out.println();
            
//TODO: write method to convert to screen coordinates
//            v1 = MatrixUtils.cameraPointToScreenPoint(v1);
//            v2 = MatrixUtils.cameraPointToScreenPoint(v2);
//            v3 = MatrixUtils.cameraPointToScreenPoint(v3);
//            v4 = MatrixUtils.cameraPointToScreenPoint(v4);
            
            
            //NOTE(revan): debug prints
            float[] poseMatrix = result.getPose().getData();
            Log.d(LOGTAG, "Pose:");
            for (int i = 0; i < poseMatrix.length; i++) {
            	System.out.print(poseMatrix[i] + " ");
            }
            System.out.println();
            
            Log.d(LOGTAG, "modelViewMatrix:");
            for (int i = 0; i < modelViewMatrix.length; i++) {
            	System.out.print(modelViewMatrix[i] + " ");
            }
            System.out.println();
            
            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];
            
            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                OBJECT_SCALE_FLOAT);
            Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
            
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            
            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);
            
            SampleUtils.checkGLError("Render Frame");
            
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        mRenderer.end();
    }
    
    
    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }
}
