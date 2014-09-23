package visualmimo.com.visualmimo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;

import visualmimo.com.visualmimo.camera.CameraView;


public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        CameraView cameraView = new CameraView(this);
        FrameLayout panel = (FrameLayout) findViewById(R.id.cameraView);
        panel.addView(cameraView);
    }
}
