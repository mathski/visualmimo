package visualmimo.com.visualmimo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import visualmimo.com.visualmimo.persistence.Frame;
import visualmimo.com.visualmimo.persistence.FrameCache;
import visualmimo.com.visualmimo.persistence.FrameOps;

public class DrawView extends SurfaceView implements Runnable {

    private final static String TAG = "?";
    private Paint textPaint = new Paint();
    volatile boolean running  = false;
    private Random random = new Random();
    SurfaceHolder holder;
    Thread thread = null;
    private FrameCache cache;
    private boolean isSubtraction;

    public DrawView(Context context, boolean isSubtraction) {
        super(context);
        this.holder = getHolder();
        setWillNotDraw(true);
        cache = FrameCache.getInstance();

        this.isSubtraction = isSubtraction;
    }

//    @Override
//    public void onDraw(Canvas canvas) {
//        Log.e("?", "Drawing Stuufs");
//        Log.e("??", "WPAH: " + FrameCache.getInstance().size());
//        // A Simple Text Render to test the display
//        canvas.drawText("Hello World!", 50, 50, textPaint);
//    }
//
//    public void drawImage(SurfaceHolder realHolder) {
//        int count = 0;
//        while(true) {
//
//            boolean isChanged = .isChanged();
//            if(holder.getSurface().isValid()){
//                Log.e("??", "DRAW IT");
//                Canvas canvas = realHolder.lockCanvas();
//                //... actual drawing on canvas
//
//                canvas.drawText("Count: " + count++, 50, 50, textPaint);
//
//                holder.unlockCanvasAndPost(canvas);
//            }
//            break;
//        }
//    }
//
//    private void tryDrawing(SurfaceHolder holder) {
//        Log.e(TAG, "Trying to draw...");
//
//        Canvas canvas = holder.lockCanvas();
//        if (canvas == null) {
//            Log.e(TAG, "Cannot draw onto the canvas as it's null");
//        } else {
//            drawMyStuff(canvas);
//            holder.unlockCanvasAndPost(canvas);
//        }
//    }
//
//    private void drawMyStuff(final Canvas canvas) {
//        Log.e(TAG, "Drawing...");
//        //canvas.drawRGB(255, 128, 128);
//        canvas.drawText("Hello THUNDE!", 50, 50, textPaint);
//    }

    public void onResumeMySurfaceView(){
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void onPauseMySurfaceView(){
        boolean retry = true;
        running = false;
        while(retry){
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run(){
        while(running){
            if(holder.getSurface().isValid()){
                Canvas canvas = holder.lockCanvas();
                Frame first = cache.getRecentFrame(0);
                Frame second = cache.getRecentFrame(1);

                if( first != null && second != null) {

                    Frame resultFrame;

                    if(isSubtraction) {
                        second = FrameOps.intensify(second);
                        resultFrame = FrameOps.frameSubtraction(first, second);
                    } else {
                        second = FrameOps.scale(second);
                        resultFrame = FrameOps.frameDivision(first, second);
                    }


                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    YuvImage yuvImage = new YuvImage(resultFrame.getRaw(), ImageFormat.NV21, resultFrame.getWidth(), resultFrame.getHeight(), null);
                    yuvImage.compressToJpeg(new Rect(0, 0, resultFrame.getWidth(), resultFrame.getHeight()), 50, out);
                    byte[] imageBytes = out.toByteArray();

                    Matrix matrix = new Matrix();
                    matrix.setRotate(90);
                    matrix.postTranslate(resultFrame.getHeight(), 0);

                    Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    //Bitmap rotated = Bitmap.createBitmap(image, 0,0, x.getWidth(), x.getHeight(), matrix, true);

//                    Log.e("** " , "Frame is alive!! " + resultFrame.getRaw().length);
//                    Log.e("**", "The Map: " + image);
                    canvas.drawBitmap(image, matrix, new Paint());
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
