package com.android.visualmimo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TableLayout;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by joeb3219 on 11/22/2015.
 */
public class Whiteboard extends Activity {

    enum WhiteboardTool{

        PENCIL(R.drawable.pencil, 4, 0, "Draw"),
        ERASER(R.drawable.eraser, 4, 1, "Eraser");

        public int x, y, icon, height = -1, width = -1;
        public String name;
        private WhiteboardTool(int drawable, int x, int pos, String name){
            this.icon = drawable;
            this.x = x;
            this.y = pos;
            this.name = name;
        }

        public boolean clickInBounds(float cX, float cY){
            if(cX < x || cX > x + width) return false;
            if(cY < y || cY > y + height) return false;
            return true;
        }

    }

    private Board board;
    private static final int SERVERPORT = 9090;
    private static final String SERVER_IP = "";
    private float touchDownX, touchDownY = 0;
    private final static float TOUCH_THRESHHOLD = 10;
    private boolean isOnClick = false;
    private Socket socket;
    public static String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.whiteboard_activity);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Drawable d;

        String imageId = getIntent().getStringExtra("id");
        if("15".equals(imageId)){
            d = getResources().getDrawable(R.drawable.fifteen);
        }else if("5".equals(imageId)){
            d = getResources().getDrawable(R.drawable.five);
        }else return;

        board = new Board(this, d, size);
        board.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) { 
                return touch(event);
            }
        });
        addContentView(board, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        try {
            socket = IO.socket("http://162.243.19.167:9090");
        }catch(Exception e){e.printStackTrace();}
        socket.connect();
        socket.on("vmimo", onDataTransfer);
    }

    public boolean touch(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = event.getX();
                touchDownY = event.getY();
                isOnClick = true;
                for(WhiteboardTool tool : WhiteboardTool.values()){
                    if(tool.clickInBounds(touchDownX, touchDownY)){
                        board.currentTool = tool;
                        draw();
                        return true;
                    }
                }
                board.currentPath = new Path(new ArrayList<Coordinate>());
                return true;
            case MotionEvent.ACTION_UP:
                if(board.currentPath == null || board.currentPath.coordinates.size() == 0) return true;
                try {
                    socket.emit("vmimo", new JSONObject(board.currentPath.toString()));
                    board.paths.add(board.currentPath);
                }catch(Exception e){e.printStackTrace();}
                board.currentPath = null;
                return true;
            case MotionEvent.ACTION_MOVE:
                if ((Math.abs(touchDownX - event.getX()) > TOUCH_THRESHHOLD || Math.abs(touchDownY - event.getY()) > TOUCH_THRESHHOLD)) {
                    isOnClick = false;
                    if(board.currentPath == null) board.currentPath = new Path(new ArrayList<Coordinate>());
                    board.currentPath.coordinates.add(new Coordinate(event.getX(), event.getY()));
                    if(board.currentPath.coordinates.size() % 4 == 0){
                        try {
                            socket.emit("vmimo", new JSONObject(board.currentPath.toString()));
                            board.paths.add(board.currentPath);
                        }catch(Exception e){e.printStackTrace();}
                        board.currentPath = new Path(new ArrayList<Coordinate>(Arrays.asList(board.currentPath.coordinates.get(board.currentPath.coordinates.size() - 1))));
                    }
                    draw();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void draw(){
        board.draw();
    }

    public void onDestroy() {
        super.onDestroy();
        if(socket == null) return;
        socket.disconnect();
        socket.close();
    }

    private Emitter.Listener onDataTransfer = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data;
                    if(args[0] instanceof JSONObject){
                        data = (JSONObject) args[0];
                    }
                    else {
                        Log.d("vmimo", (String) args[0]);
                        String s = (String) args[0];
                        if("cmd:clear".equals(s)){
                            board.paths = new ArrayList<Path>();
                            draw();
                            return;
                        }
                        if(s.contains("cmd:colour")){
                            int colour = Integer.valueOf(s.split(":")[2]);
                            Path._DEFAULT_COLOUR = colour;
                            return;
                        }else if(s.contains("cmd:id")){
                            id = s.split(":")[2];
                            return;
                        }else if(s.contains("cmd:size")){
                            String param = s.split(":")[2];
                          //  board.resize(Integer.parseInt(param.split("x")[1]), Integer.parseInt(param.split("x")[0]));
                            return;
                        }
                        try{data = new JSONObject(s);}catch(Exception e){e.printStackTrace(); return;}
                    }
                    try {
                        if(data.isNull("colour") || !data.has("colour") || data.isNull("coordinates") || !data.has("id")) return;
                        if(data.getString("id").equals(id)) return; //This device drew it.
                        ArrayList<Coordinate> pathCoords = new ArrayList<Coordinate>();
                        int colour = data.getInt("colour");
                        JSONArray coordinates = data.getJSONArray("coordinates");
                        for(int i = 0; i < coordinates.length(); i += 2){
                            pathCoords.add(new Coordinate((float) coordinates.getDouble(i) * Board._X_SCALE, (float) coordinates.getDouble(i + 1) * Board._Y_SCALE));
                        }
                        Path resultantPath = new Path(pathCoords);
                        resultantPath.colour = colour;
                        board.paths.add(resultantPath);
                        draw();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
        }
    };

}
