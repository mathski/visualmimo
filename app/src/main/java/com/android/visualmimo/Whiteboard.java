package com.android.visualmimo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableLayout;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by joeb3219 on 11/22/2015.
 */
public class Whiteboard extends Activity {

    private Board board;
    private static final int SERVERPORT = 9090;
    private static final String SERVER_IP = "162.243.19.167";
    private com.github.nkzawa.socketio.client.Socket socket;
    private float touchDownX, touchDownY = 0;
    private final static float TOUCH_THRESHHOLD = 10;
    private boolean isOnClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.whiteboard_activity);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        Drawable d = getResources().getDrawable(R.drawable.fifteen);
        d.setBounds(0, 0, size.x, size.y);

        board = new Board(this, d);
        board.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return touch(event);
            }
        });
        addContentView(board, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        try {
            socket = IO.socket("http://" + SERVER_IP + ":" + SERVERPORT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.connect();

        socket.on("vmimo", onDataTransfer);
        socket.connect();
    }


    public boolean touch(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                board.currentPath = new Path(new ArrayList<Coordinate>());
                //board.paths.add(new Path(new ArrayList<Coordinate>()));
                touchDownX = event.getX();
                touchDownY = event.getY();
                isOnClick = true;
                return true;
            case MotionEvent.ACTION_UP:
                try {
                    socket.emit("vmimo", new JSONObject(board.currentPath.toString()));
                }catch(Exception e){e.printStackTrace();}
                board.currentPath = null;
                return true;
            case MotionEvent.ACTION_MOVE:
                if ((Math.abs(touchDownX - event.getX()) > TOUCH_THRESHHOLD || Math.abs(touchDownY - event.getY()) > TOUCH_THRESHHOLD)) {
                    isOnClick = false;
                    board.currentPath.coordinates.add(new Coordinate(event.getX(), event.getY()));
                    if(board.currentPath.coordinates.size() % 4 == 0){
                        try {
                            socket.emit("vmimo", new JSONObject(board.currentPath.toString()));
                        }catch(Exception e){e.printStackTrace();}
                        board.paths.add(board.currentPath);
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
        socket.disconnect();
    }

    private Emitter.Listener onDataTransfer = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data;
                    if(args[0] instanceof JSONObject) data = (JSONObject) args[0];
                    else {
                        Log.d("vmimo", (String) args[0]);
                        try{data = new JSONObject((String) args[0]);}catch(Exception e){e.printStackTrace(); return;}
                    }
                    try {
                        ArrayList<Coordinate> pathCoords = new ArrayList<Coordinate>();
                        int colour = data.getInt("colour");
                        JSONArray coordinates = data.getJSONArray("coordinates");
                        for(int i = 0; i < coordinates.length(); i += 2){
                            pathCoords.add(new Coordinate((float) coordinates.getDouble(i), (float) coordinates.getDouble(i + 1)));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
