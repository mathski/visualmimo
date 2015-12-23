package edu.rutgers.vmimo.socket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Random;

public class SocketConnection extends ServerSocket implements Runnable{

	public static String _IP_ADDRESS;
	public boolean serverRunning = false;
	public Socket currentClient = null;
	private OutputStreamWriter osw = null;
	
	public SocketConnection(int port) throws IOException{
		super(port);
		serverRunning = true;
		setSoTimeout(1000);
	}
	
	public static String getIP(){
		if(_IP_ADDRESS != null) return _IP_ADDRESS;
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader((new URL("http://checkip.amazonaws.com")).openStream()));
			return ( _IP_ADDRESS = in.readLine() );
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	public void invalidateCurrentClient(){
		try{
			if(currentClient != null) currentClient.close();
			if(osw != null){
				osw.close();
				osw = null;
			}
		}catch(Exception e){e.printStackTrace();}
		currentClient = null;
	}
	
	public void sendMessage(String message){
		if(currentClient == null) return;
		try {
	        if(osw == null) osw = new OutputStreamWriter(currentClient.getOutputStream(), "UTF-8");
	        osw.write(message + "\r");
	        osw.flush();
	    }catch(SocketException e){ //A symptom of the socket being closed by one of the ends.
	    	invalidateCurrentClient();
	    }catch(UnsupportedEncodingException e){e.printStackTrace();}
		catch(IOException e){e.printStackTrace();}
	}
	
	public void run(){
		BufferedReader reader = null;
		while(serverRunning){
			try{
				if(currentClient == null){
					currentClient = accept();
					System.out.println("Connected to client: " + currentClient.getRemoteSocketAddress());
					reader = new BufferedReader(new InputStreamReader(currentClient.getInputStream()));
					continue;
				}
				while (reader.ready()){
					System.out.println(reader.readLine());
				}
	         }catch(SocketTimeoutException s){ //Timeouts aren't actually errors since we time out every second (to allow accept() to die).
	         }catch(IOException e){
	            e.printStackTrace();
	            break;
	         }
	      }
	      try{
	    	  if(osw != null) osw.close();
	    	  if(reader != null) reader.close();
	    	  invalidateCurrentClient();
	    	  close();
	      }catch(Exception e){e.printStackTrace();}
	}
	
}
