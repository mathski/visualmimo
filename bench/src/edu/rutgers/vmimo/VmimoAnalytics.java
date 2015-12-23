package edu.rutgers.vmimo;

import java.io.IOException;
import java.util.Scanner;

import edu.rutgers.vmimo.socket.SocketConnection;

public class VmimoAnalytics {

	public static int startingFPS, endingFPS, startingDelta, endingDelta, imagesPerDelta, deltaStep, imageId;
	public static final Scanner in = new Scanner(System.in);
	public static MessagePack messagePack;
	public static SocketConnection socket;
	
	/*
	 * 
	 * Proposed Workflow:
	 * 
	 * - User begins bench by starting Java program.
	 * - Bench requests a port number, then instantiates Socket Server (Bench itself is the server) on given port.
	 * - User is then shown login info (IP + port) to input into phone -> phone will join session
	 * - Upon detecting a new socket client, bench will ask if device is the intended test device
	 * 		- If no, disconnect it & wait.
	 * - Bench now ignores all other incoming clients -> auto DC them 
	 * - Bench waits for user input of command
	 * - Commands:
	 * 		- flushCache
	 * 			- Flushes the cached messages used in embedding images (and then generates new ones)
	 * 		- newDevice
	 * 			- Disconnects current test device & looks for a new one (return to step 4)
	 * 		- test
	 * 			- params: -startingFps -endingFps -startingDelta -endingDelta -imagesPerDelta -deltaStep -imageId
	 * 			- Will begin the code to test if 
	 * 
	 */
	
	public static void main(String[] args){
		Thread socketThread = null;
		messagePack = new MessagePack();
		try{
			try
		      {
		         socket = new SocketConnection(getIntInput("Enter a port number"));
		         socketThread = new Thread(socket, "Socket Thread");
		         socketThread.start();
		      }catch(Exception e){e.printStackTrace();}
			System.out.println("Enter this login key into your phone: " + SocketConnection.getIP() + ":" + socket.getLocalPort());
			System.out.println("Enter this login key into your phone: " + socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort());
		}catch(Exception e){e.printStackTrace();}
		String command = "";
		while( !(command = getStringInput("Enter a command or [q]uit")).equals("q") && !command.equals("quit") ){
			if(command.equalsIgnoreCase("help")) loadHelpMessages();
			else if(command.equalsIgnoreCase("flushCache")) flushCache();
			else if(command.equalsIgnoreCase("newDevice")) newDevice();
			else if(command.startsWith("test")) initializeTest(command.substring(5).split(" "));
			else System.out.println("Command not recognized: " + command);
		}
		try{
			System.out.println("Shutting down server");
			socket.serverRunning = false;
			socket.invalidateCurrentClient();
			if(!socket.isClosed()) socket.close();
			if(socketThread != null){
				socketThread.interrupt();
				socketThread.join();
			}
		}catch(Exception e){e.printStackTrace();}
	}

	private static void flushCache(){
		System.out.println("Flushing messages cache.");
		messagePack.flush();
		System.out.println("Creating new message cache.");
		messagePack.build();
	}
	
	private static void newDevice(){
		System.out.println("Ending socket connection with previous device.");
		socket.invalidateCurrentClient();
	}
	
	
	//Example of params: 1 12 0 50 200 1 5
	/**
	 * Params array expected in the following order:
	 * startingFps, endingFps, startingDelta, endingDelta, imagesPerDelta, deltaStep, imageId
	 * @param params
	 */
	private static void initializeTest(String[] params){
		if(params.length < 7){
			System.out.println("Not enough parameters: expected 7, got " + params.length + ".");
			return;
		}else if(socket.currentClient == null){
			System.out.println("No testing device connected.");
			return;
		}
		int startingFPS = Integer.parseInt(params[0]);
		int endingFPS = Integer.parseInt(params[1]);
		int startingDelta = Integer.parseInt(params[2]);
		int endingDelta = Integer.parseInt(params[3]);
		int imagesPerDelta = Math.max(Integer.parseInt(params[4]), messagePack._PACK_SIZE); //At least one per image
		int deltaStep = Integer.parseInt(params[5]);
		int imageID = Integer.parseInt(params[6]);
		
		for(int fps = startingFPS; fps < endingFPS; fps ++){
			for(int delta = startingDelta; delta < endingDelta; delta += deltaStep){
				int picturesPerMessage = imagesPerDelta / messagePack._PACK_SIZE;
				for(int picturesTaken = 0; picturesTaken < imagesPerDelta; picturesTaken += picturesPerMessage){
					socket.sendMessage("test=true;imgcount=" + picturesPerMessage);
				}
			}
		}
	
	}
	
	private static void loadHelpMessages(){
		System.out.println("Valid commands: ");
		System.out.println("flushCache -> flushes old caches messages and regenerates new ones.");
		System.out.println("newDevice -> disconnects current test device and looks for a new one.");
		System.out.println("test [params: -startingFps -endingFps -startingDelta -endingDelta -imagesPerDelta -deltaStep -imageId] -> begins an accuracy test.");
	}
	
	public static String getStringInput(String message){
		System.out.print(message + ": ");
		return in.nextLine();
	}
	
	public static int getIntInput(String message){
		while(true){
			System.out.print(message + ": ");
			try {
				return Integer.parseInt(in.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("Integer expected.");
			}
		}
	}	
	
}
