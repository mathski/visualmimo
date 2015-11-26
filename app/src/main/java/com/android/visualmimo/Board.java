package com.android.visualmimo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
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

    public Board(Context context, Drawable background) {
        super(context);
        this.context = context;
        paint = new Paint();
        this.background = background;
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
    }

}
