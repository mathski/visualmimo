package edu.rutgers.vmimo.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class SocketConnection extends ServerSocket implements Runnable{

	public static String _IP_ADDRESS;
	public boolean serverRunning = false;
	public Socket currentClient = null;
	private OutputStreamWriter osw = null;
	private long timeOfLastMessage = 0;
	private String lastMessage = "";
	final Object lockObject = new Object();
	
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
	
	/*public String getNextMessageOrWait(){
		long savedSastMessageTime = timeOfLastMessage;
		while(serverRunning && savedSastMessageTime < this.timeOfLastMessage){
			System.out.println("Waiting for a new message");
			if(currentClient == null || currentClient.isClosed()) break;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		return lastMessage;
	}*/
	
	public String getNextMessageOrWait(){
		synchronized(lockObject){
			try {
				lockObject.wait();
				return lastMessage;
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		return "Error waiting for last message";
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
				synchronized(lockObject){
					while (reader.ready()){
						timeOfLastMessage = System.currentTimeMillis() / 1000L;
						lastMessage = reader.readLine();
						System.out.println("Received: " + lastMessage);
						lockObject.notify();
					}
				}
	         }catch(SocketTimeoutException s){ //Timeouts aren't actually errors since we time out every second (to allow accept() to die).
	         }catch(IOException e){
	            e.printStackTrace();
	            break;
	         }
	      }
		  synchronized(lockObject){
			  lockObject.notify(); //We're dead in the water, stop waiting.
		  }
	      try{
	    	  if(osw != null) osw.close();
	    	  if(reader != null) reader.close();
	    	  invalidateCurrentClient();
	    	  close();
	      }catch(Exception e){e.printStackTrace();}
	}
	
}
