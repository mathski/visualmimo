package com.android.visualmimo.camera;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.android.visualmimo.persistence.MIMOFrame;
import com.android.visualmimo.persistence.FrameOps;

/**
 * Attached to drawview, handles subtraction and division of MIMOFrames and
 * preparation for output.
 * @author alexio
 */
public class ImageProcessing extends Thread {
	
	volatile boolean stopRequest = false;
	volatile boolean running = false;
	
	protected int outputWidth;
	protected int outputHeight;
	
	protected View view;
	Thread thread;
	
	// scale and translation applied to the canvas
	protected double scale;
	protected double tranX,tranY;
	
	/**
	 * Lock used ever reading or writing to display related data. Use this to ensure
	 * that the processing() function and render() function's intefere with each other
	 */
	protected final Object lockGui = new Object();
	
	/**
	 * Lock used when converting the video stream.  Should not need to be used by the user.
	 */
	protected final Object lockConvert = new Object();
	
	Matrix matrix;
	public void initView(View view) {
		synchronized (lockGui) {
			this.view = view;
		}
		// start the thread
		running = true;
		start();
	}
	
	public void initSizeFromCamera(Camera camera) {
		if(camera != null) {
			Camera.Size size = camera.getParameters().getPreviewSize();
			outputWidth = size.width;
			outputHeight = size.height;
			
	        matrix = new Matrix();
	        matrix.setRotate(90);
	        matrix.postTranslate(outputHeight, 0);
		}
	}
	
	/**
	 * 
	 * @param canvas Use to draw results to
	 */
	public void draw(Canvas canvas) {
		synchronized (lockGui) {
			// the process class could have been swapped
			if( image == null ) {
				Log.e("???", "IS IMAGE NULL???");
				return;
			}
//
//			int w = view.getWidth();
//			int h = view.getHeight();
//
//			// fill the window and center it
//			double scaleX = w/(double)outputWidth;
//			double scaleY = h/(double)outputHeight;
//
//			scale = Math.min(scaleX,scaleY);
//			tranX = (w-scale*outputWidth)/2;
//			tranY = (h-scale*outputHeight)/2;
//
//			canvas.translate((float)tranX,(float)tranY);
//			canvas.scale((float)scale,(float)scale);

			render(canvas, scale);
		}
	}
	
	// output image which is modified by processing thread
	private Bitmap output;
	// output image which is displayed by the GUI
	private Bitmap outputMap;
	// storage used during image convert
	private byte[] storage;
	
	String currentType;
	
	public void setProcessingType(String type) {
		this.currentType = type;
	}
	
	public void process(MIMOFrame first_frame, MIMOFrame second) {
		MIMOFrame result_frame;
		if( currentType == "Subtraction") {
			Log.e("??", "& Processing is subtracting!!");
		//second = FrameOps.intensify(second);
			result_frame = FrameOps.frameSubtraction(first_frame, second);
		} else {
		//second = FrameOps.scale(second);
			Log.e("??", "& Processing is dividing!!");
			result_frame = FrameOps.frameDivision(first_frame, second);
		}
		
		//TODO(revan): pretty sure I broke this by changing the the image format to RGB888.
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		YuvImage yuvImage = new YuvImage(result_frame.getRaw(), ImageFormat.NV21, result_frame.getWidth(), result_frame.getHeight(), null);
		yuvImage.compressToJpeg(new Rect(0, 0, result_frame.getWidth(), result_frame.getHeight()), 50, out);
		byte[] imageBytes = out.toByteArray();
		
		// recycle old bitmaps for performance
		if (output != null) {
			output.recycle();
		}
		
		output = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
		synchronized ( lockGui ) {
			Bitmap tmp = output;
			output = outputMap;
			outputMap = tmp;
		}
	}
	
	public void render(Canvas canvas, double scale) {
		synchronized ( lockGui ) {
			Log.e("???", "RENDER!!");
			canvas.drawBitmap(outputMap,matrix, new Paint());
		}
	}
	
	MIMOFrame image;
	MIMOFrame second_image;
	public void convertPreview(MIMOFrame first_frame, MIMOFrame second_frame, Camera camera) {
		if( thread == null )
			return;
		
		if( first_frame == null || second_frame == null)
			return;
		
		image = first_frame;
		second_image = second_frame;
		thread.interrupt();
	}
	
	public void stopProcessing() {
		if( thread == null )
			return;

		stopRequest = true;
		while( running ) {
			// wake the thread up if needed
			thread.interrupt();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
	}

    MIMOFrame image2;
    MIMOFrame image3;
	@Override
	public void run() {
		thread = Thread.currentThread();
		while( !stopRequest ) {
			synchronized ( thread ) {
				try {
					wait();
					if( stopRequest )
						break;
				} catch (InterruptedException e) {}
			}
			
			synchronized ( lockConvert ) {
				MIMOFrame tmp = image;
				image = image2;
				image2 = tmp;
				
				MIMOFrame tmp2 = second_image;
				second_image = image3;
				image3 = tmp2;
			}
			
			process(image2, image3);
			
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
        			view.invalidate();
                }
            });
		}
		running = false;
	}
	
	Context context;
	
	public void setContext(Context context) {
		this.context = context;
	}
}
