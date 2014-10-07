package com.android.visualmimo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import com.android.visualmimo.persistence.Frame;
import com.android.visualmimo.persistence.FrameCache;
import com.android.visualmimo.persistence.FrameOps;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private final static String TAG = "DrawView";
    private Paint textPaint = new Paint(); 
    
    private double history[] = new double[10];
	private int historyNum = 0;

	private long previousTime = 0;
	
	ImageProcessing processing;
	SurfaceHolder holder;
    
    public enum ProcessingType {
    	Subtraction("Subtraction"), Division("Division");
    	private String value;
    	
    	private ProcessingType(String value) {
    		this.value = value;
    	}
    }
    
    ProcessingType currentProcessing;

    public DrawView(Context context) {
        super(context);
        
		textPaint.setARGB(255, 200, 0, 0);
		textPaint.setTextSize(60);
        this.holder = getHolder();
        this.holder.addCallback(this);
    }

    @Override
    public void onDraw(Canvas canvas) {
    	canvas.save();
    	Log.e("***", "DRAW VIEW CANVAS");
    	if(processing != null) {
    		Log.e(TAG, "Proccessing frames");
    		processing.draw(canvas);
    	}
    	
    	canvas.restore();
    	//TODO - show FPS
    	//canvas.drawText(String.format("FPS = %5.2f",getAverageFps()), 50, 50, textPaint);
    }
    
    public double getAverageFps() {
		long current = System.currentTimeMillis();
		long elapsed = current - previousTime;
		previousTime = current;
		history[historyNum++] = 1000.0/elapsed;
		historyNum %= history.length;

		double meanFps = 0;
		for( int i = 0; i < history.length; i++ ) {
			meanFps += history[i];
		}
		
		return meanFps /= history.length;
    }
    
    public void setProcessor(ImageProcessing processor) {
    	this.processing = processor;
    	processing.initView(this);
    }
    
    public void setProcessingType(ProcessingType type) {
    	currentProcessing = type;
    	this.processing.setProcessingType(currentProcessing.value);
    }
    
    public String getProcessingType() {
    	return currentProcessing.value;
    }

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		setWillNotDraw(false);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		
        setWillNotDraw(false);
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}
}
