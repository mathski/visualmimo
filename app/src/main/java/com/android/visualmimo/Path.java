package com.android.visualmimo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by joeb3219 on 11/22/2015.
 */
public class Path {

    public static int _DEFAULT_COLOUR = Color.WHITE;
    public int colour = _DEFAULT_COLOUR;
    public ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();

    public Path(ArrayList<Coordinate> coordinates){
        this.coordinates = coordinates;
        colour = _DEFAULT_COLOUR;
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
        if(coordinates.size() == 0) return "{}";
        String JSON = "{";
        JSON += "\"colour\":" + colour + ",";
        JSON += "\"id\":" + Whiteboard.id + ",";
        JSON += "\"coordinates\":[";
        for(Coordinate c : coordinates){
            JSON += ((double) c.x / Board._X_SCALE) + "," + ((double) c.y / Board._Y_SCALE) + ",";
        }
        JSON = JSON.substring(0, JSON.length() - 2);
        JSON += "]}";
        return JSON;
    }

}
