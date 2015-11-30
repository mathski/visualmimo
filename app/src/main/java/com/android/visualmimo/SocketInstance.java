package com.android.visualmimo;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Created by joeb3219 on 11/27/2015.
 */
public class SocketInstance {
    private static SocketInstance _INSTANCE = new SocketInstance();
    private Socket socket;
    private static final String _SERVER_ADDRESS = "http://162.243.19.167:9090";

    public static SocketInstance getInstance() {
        if(_INSTANCE == null) _INSTANCE = new SocketInstance();
        return _INSTANCE;
    }

    public Socket getSocket(){return socket;}

    private SocketInstance() {
        try {
            socket = IO.socket("http://162.243.19.167:9090");
            socket = socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
