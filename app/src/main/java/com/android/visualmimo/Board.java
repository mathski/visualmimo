package com.android.visualmimo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by joeb3219 on 11/22/2015.
 */
public class Board extends View {
    public static Context context;
    private Paint paint;
    private Drawable background;
    public ArrayList<Path> paths = new ArrayList<Path>();
    public Path currentPath;
    public Whiteboard.WhiteboardTool currentTool = Whiteboard.WhiteboardTool.PENCIL;
    public static int width = 560, height = 448;
    public static float _X_SCALE = 2, _Y_SCALE = 2;
    private Point screenSize;

    public Board(Context context, Drawable background, Point screenSize) {
        super(context);
        this.context = context;
        paint = new Paint();
        this.screenSize = screenSize;

        this.background = background;
        resize(height, width);
    }

    public void resize(int height, int width){
        this.height = height;
        this.width = width;

        float ratio = width / height;
        _X_SCALE = screenSize.x * 1.0f / width;
        _Y_SCALE = screenSize.y * 1.0f / height;
        background.setBounds(0, 0, (int) ((width * _X_SCALE)), (int) ((height * _Y_SCALE)));
        /*if(width > screenSize.x || ( screenSize.x / width < screenSize.y / height )) {
            _X_SCALE = screenSize.x * 1.0f / width;
            _Y_SCALE = _X_SCALE * ratio;
            background.setBounds(0, 0, (int) (width * _X_SCALE), (int) (height * _Y_SCALE));
        }else{
            _Y_SCALE = screenSize.y * 1.0f / height;
            _X_SCALE = _Y_SCALE / ratio;
            background.setBounds(0, 0, (int) ((width * _X_SCALE)), (int) ((height * _Y_SCALE)));
        }*/

        Log.d("vmimo", background.getBounds().right + "x" + background.getBounds().bottom);

        draw();
    }

    public void draw(){
        invalidate();
    }

    public void onDraw(Canvas canvas){
        drawBoard(canvas);
    }

    private void drawBoard(Canvas canvas){
        canvas.drawColor(Color.WHITE);
        background.draw(canvas);
        for(Path p : paths) p.draw(canvas, paint);
        if(currentPath != null) currentPath.draw(canvas, paint);
        drawTools(canvas);
    }

    private void drawTools(Canvas canvas){
        for(Whiteboard.WhiteboardTool tool : Whiteboard.WhiteboardTool.values()){
            paint.setAlpha(100);
            if(currentTool == tool) paint.setAlpha(255);
            Bitmap bm = ((BitmapDrawable) getResources().getDrawable(tool.icon)).getBitmap();
            if(tool.height == -1){
                tool.height = tool.width = bm.getHeight();
                tool.y = (tool.y * (tool.height + 8)) + 8;
            }
            canvas.drawBitmap(bm, tool.x, tool.y, paint);
        }
        paint.setAlpha(255);
    }

}
