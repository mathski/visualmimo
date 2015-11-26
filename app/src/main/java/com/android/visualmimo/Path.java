package com.android.visualmimo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by joeb3219 on 11/22/2015.
 */
public class Path {

    public int colour = Color.RED;
    public ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();

    public Path(ArrayList<Coordinate> coordinates){
        this.coordinates = coordinates;
    }

    public void draw(Canvas canvas, Paint paint){
        paint.setColor(colour);
        paint.setStrokeWidth(4f);
        for(int i = 1; i < coordinates.size(); i ++) {
            Coordinate c = coordinates.get(i);
            Coordinate cLast = coordinates.get(i - 1);
            canvas.drawLine(c.x, c.y, cLast.x, cLast.y, paint);
        }
    }

    public String toString(){
        String JSON = "{";
        JSON += "\"colour\":" + colour + ",";
        JSON += "\"coordinates\":[";
        for(Coordinate c : coordinates){
            JSON += ((double) c.x) + "," + ((double) c.y) + ",";
        }
        JSON = JSON.substring(0, JSON.length() - 2);
        JSON += "]}";
        return JSON;
    }

}