package visualmimo.com.visualmimo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import visualmimo.com.visualmimo.camera.CameraView;
import visualmimo.com.visualmimo.camera.DrawView;


public class MainActivity extends Activity {

    private FrameLayout panel;
    private DrawView subtractView;
    private DrawView divisionView;
    private CameraView cameraView;
    private int originalHeight;
    private int originalWidth;
    private Button switchButton;


    private int viewStatus = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        panel = (FrameLayout) findViewById(R.id.cameraView);
        switchButton = (Button) findViewById(R.id.switch_button);
        cameraView = new CameraView(this);
        subtractView = new DrawView(this, true);
        divisionView = new DrawView(this, false);


        panel.addView(subtractView);
        panel.addView(divisionView);
        panel.addView(cameraView);
        panel.bringChildToFront(cameraView);
        //panel.invalidate();
    }

    @Override
    public void onPause() {
        super.onPause();
        subtractView.onPauseMySurfaceView();
        divisionView.onPauseMySurfaceView();
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewSize();
        switchView(null);
        subtractView.onResumeMySurfaceView();
        divisionView.onResumeMySurfaceView();
    }

    public void getViewSize() {
        originalHeight = cameraView.getLayoutParams().height;
        originalWidth = cameraView.getLayoutParams().width;
    }

    public void subtractionViewSize(int width, int height) {
        subtractView.getLayoutParams().height = height;
        subtractView.getLayoutParams().width = width;
        subtractView.requestLayout();
    }

    public void divisionViewSize(int width, int height) {
        divisionView.getLayoutParams().height = height;
        divisionView.getLayoutParams().width = width;
        divisionView.requestLayout();
    }


    public void cameraViewSize(int width, int height) {
        cameraView.getLayoutParams().height = height;
        cameraView.getLayoutParams().width = width;
        cameraView.requestLayout();
    }


    public void switchView(View view) {

        switch(viewStatus) {
            case 0: //Camera
                Log.e("*", "CAMERA VIEW");
                cameraViewSize(originalWidth, originalHeight);
                divisionViewSize(0 ,0 );
                subtractionViewSize(0,0);
                switchButton.setText("Camera");
                viewStatus++;
                break;
            case 1: //Subtraction
                Log.e("*", "SUBTACT VIEW");
                cameraViewSize(0, 0);
                divisionViewSize(0 ,0 );
                subtractionViewSize(originalWidth, originalHeight);
                switchButton.setText("Subtraction");
                viewStatus++;
                break;
            case 2: //Division
                Log.e("*", "DIVIDE VIEW");
                cameraViewSize(0,0);
                divisionViewSize(originalWidth, originalHeight);
                subtractionViewSize(0,0);
                switchButton.setText("Division");
                viewStatus = 0;
                break;
        }



//        if(isCamera) {
//            Log.e("*", "Height: " + originalHeight + " , Width: " + originalWidth);
//
//            Log.e("*","MAKE IT SMALL!!");
//            cameraView.getLayoutParams().height = 0;
//            cameraView.getLayoutParams().width = 0;
//            cameraView.requestLayout();
//            isCamera = false;
//        } else {
//            Log.e("*", "Make it big");
//            cameraView.getLayoutParams().height = originalHeight;
//            cameraView.getLayoutParams().width = originalWidth;
//            Log.e("*", "Height: " + originalHeight + " , Width: " + originalWidth);
//            cameraView.requestLayout();
//            isCamera = true;
//        }
    }


}
