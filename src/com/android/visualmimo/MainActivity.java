package com.android.visualmimo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.hardware.Camera;

import com.android.visualmimo.camera.CameraView;
import com.android.visualmimo.camera.DrawView;
import com.android.visualmimo.camera.ImageProcessing;

/**
 * Video Display Activity
 */
public class MainActivity extends Activity{

    private FrameLayout layout;
    private DrawView drawView;
    private CameraView cameraView;
    private int originalHeight;
    private int originalWidth;
    private Button switchButton;
    ImageProcessing processor;
    
    private int viewStatus = 0;
    
	static {
	    System.loadLibrary("ndk1");
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        layout = (FrameLayout) findViewById(R.id.cameraView);
        switchButton = (Button) findViewById(R.id.switch_button);
        
        cameraView = new CameraView(this);
        drawView = new DrawView(this);

        layout.addView(drawView);
        layout.addView(cameraView);
        layout.bringChildToFront(cameraView);
        processor = new ImageProcessing();
        processor.setContext(this);
        drawView.setProcessor(processor);
        cameraView.setImageProcessor(processor);
        runNative();
    }
    

    @Override
    public void onPause() {
        super.onPause();
        processor.stopProcessing();
        //drawView.onPauseMySurfaceView();
    }

    @Override
    public void onResume() {
        super.onResume();
         getViewSize();
         viewStatus = 0;
         switchView(null);
        //drawView.onResumeMySurfaceView();
        //divisionView.onResumeMySurfaceView();
    }

    public void getViewSize() {
        originalHeight = cameraView.getLayoutParams().height;
        originalWidth = cameraView.getLayoutParams().width;
    }

    public void drawViewSize(int width, int height) {
        drawView.getLayoutParams().height = height;
        drawView.getLayoutParams().width = width;
        drawView.requestLayout();
    }

    public void cameraViewSize(int width, int height) {
        cameraView.getLayoutParams().height = height;
        cameraView.getLayoutParams().width = width;
        cameraView.requestLayout();
    }
    
    public void displayCamera() {
    	cameraViewSize(originalWidth, originalHeight);
        drawViewSize(0,0);
        switchButton.setText("Camera");
    }
    
    public void displayDrawView() {
    	cameraViewSize(0, 0);
        drawViewSize(originalWidth, originalHeight);
        switchButton.setText(drawView.getProcessingType());
    }

    public void switchView(View view) {

        switch(viewStatus) {
            case 0: //Camera
            	Log.e("&", "& Camera");
                displayCamera();
                viewStatus++;
                break;
            case 1:
            	drawView.setProcessingType(DrawView.ProcessingType.Subtraction);
            	Log.e("&", "&" + drawView.getProcessingType());
                displayDrawView();
                viewStatus++;
                break;
            default:
            	
            	drawView.setProcessingType(DrawView.ProcessingType.Division);
            	Log.e("&", "& " + drawView.getProcessingType());
            	displayDrawView();
            	viewStatus = 0;
            	break;
        }
    }
    
    private native void helloLog(String logThis);
    
    public void runNative(){
    	helloLog("This will log to LogCat via the native call");
    }
}
