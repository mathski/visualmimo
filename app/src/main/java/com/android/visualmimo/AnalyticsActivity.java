package com.android.visualmimo;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class AnalyticsActivity extends VuforiaActivity  implements Callback {

    private volatile boolean takePicture = false;
    private SocketClient socket;
    private Thread socketThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String serverCon = getIntent().getStringExtra("serverCon");
        setContentView(R.layout.activity_analytics_act);

        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.activity_analytics_act, null, false);
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.TRANSPARENT);
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        try{
            vuforiaAppSession.startAR();
            Log.d(LOGTAG, "Vuforia started!");
        }catch(Exception e){e.printStackTrace();}
        vuforiaAppSession.drawOnView(this, mUILayout);

        socket = new SocketClient(serverCon);
        socket.execute();
        socketThread = new Thread(socket, "Socket Thread");
        socketThread.start();
    }

    /** Create new MIMOFrame, add to FrameCache. */
    public void onQCARUpdate(State state) {
        onQCARUpdate(state, takePicture);
        if(saveCount > NUM_SAVES){
            saveCount = 0;
            takePicture = false;
        }
    }

    /** Updates message display */
    @Override
    public boolean handleMessage(Message msg) {
        ExtractedMessage extracted = (ExtractedMessage) msg.obj;
        socket.sendMessage(extracted.binary);
        Log.d(LOGTAG, "Message recovered: " + extracted.message);
        takePicture = false;
        return true;
    }

    /**
     * SocketClient class extends AsyncTask and implements Runnable
     * Creates a socket connection to the provided address (MUST be in format ip:port).
     * This class has built in protected to determine if the socket connection is open or not.
     */
    public class SocketClient extends AsyncTask<Void, Void, Void> implements Runnable{

        private String ipAddress;
        private int ipPort;
        public String response = "";
        public Socket socket = null;
        private OutputStreamWriter osw = null;;
        public BufferedReader reader;
        public boolean socketRunning = false;

        /**
         * Creates a Socket connection to the given Address.
         * @param addr String representation of IP Address in the format IP:Port (port required, eg: 128.0.0.1:4040)
         */
        SocketClient(String addr) {
            ipAddress = addr.split(":")[0];
            ipPort = Integer.parseInt(addr.split(":")[1]);
            socketRunning = true;
        }

        /**
         * Method to be run in an external Thread to reduce UI workload.
         * Essentially just checks to see if there are any new messages in the queue.
         * If any exceptions are reported (most likely IO exceptions), will close the socket as that's a sign of a closed socket, server end.
         */
        public void run(){
            while(socketRunning){
                if(socket == null || socket.isClosed()) continue;
                try{
                    String line;
                    if(reader == null) continue;
                    while (reader.ready()) {
                        String message = reader.readLine();
                        Log.d(LOGTAG, message);
                        if(message.contains("test=true")) {
                            takePicture = true;
                            saveCount = 0;
                        }
                    }
                }catch(Exception e){try{socket.close();}catch(Exception inner){inner.printStackTrace();}}
            }
        }

        /**
         * To be called via .execute(), creates the socket -- this should never be called directly by the user.
         * Execute should be called before running the thread.
         * If a ConnectException occurs, the socket could not be reached -- therefore the activity will end and bring the user back to the last activity.
         * @param arg0 AsnccTask arguments.
         * @return Void
         */
        protected Void doInBackground(Void... arg0) {
            try {
                socket = new Socket(ipAddress, ipPort);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                sendMessage("Hello world, from " + socket.getLocalAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            }catch  (ConnectException e){
                //This exception is thrown if cannot connect to the given IP:Port.
                //Therefore, we will return to the previous Activity to prompt for a correct connection key.
                finish();
            } catch (IOException e) {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }
            Log.d(LOGTAG, response);
            return null;
        }

        /**
         * Sends a message to a socket, or closes the socket if sending fails.
         * @param message Message to send to the socket.
         */
        public void sendMessage(String message){
            try {
                osw.write(message + "\r");
                osw.flush();
            }catch(IOException e){
                try{
                    socket.close();
                }catch(Exception ee){ee.printStackTrace();}
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try{
            if(socket.socket != null){
                socket.socketRunning = false;
                socket.socket.close();
                if(socket.osw != null) socket.osw.close();
                socket.reader.close();
                socketThread.interrupt();
                socketThread.join();
            }
        }catch(Exception e){e.printStackTrace();}
    }

}
