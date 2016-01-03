package edu.rutgers.vmimo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class MessagePack {

	public static final int _PACK_SIZE = 25;
	public static final int _MESSAGE_LENGTH_CHARS = 11;
	public static final int _MESSAGE_LENGTH_BITS = 80;
	public Random rand = new Random();
	public int[] binaryInaccuracies = new int[_MESSAGE_LENGTH_BITS];
	public String[] messages = new String[_PACK_SIZE];
	public String[] binaryMessages = new String[_PACK_SIZE];
	private static final String _ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
	public static final String _MESSAGES_SAVE_PATH = "message.txt";
	public int testedAccuracies = 0;
	
	public MessagePack(){
		build();
		savePack();
	}
	
	/**
	 * Creates a messagepack with messages stored in a file.
	 * If the file doesn't have enough messages (< _PACK_SIZE) or is null, will create a new randomly generated pack.
	 * @param file File to load from.
	 */
	public MessagePack(File file){
		int currentPos = 0;
		try{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				messages[currentPos] = line;
				binaryMessages[currentPos] = convertStringToBinary(messages[currentPos]);
				currentPos ++;
			}
			bufferedReader.close();
		}catch(IOException e){e.printStackTrace(); currentPos = 0;}
		if(currentPos != _PACK_SIZE){
			flush();
			build();
			savePack();
		}
	}

	public void generateMissedBitsReport(File dir){
		final int _STEP_SIZE = 50;
		int min = 0;
		int max = testedAccuracies;
		
		//Text based output
		ArrayList<String> missedBits = new ArrayList<String>();
		String s = "";
		for(int i = 0; i < binaryInaccuracies.length; i ++){
			s += "[" + binaryInaccuracies[i] + "," + ( ((binaryInaccuracies[i]-min) * 1.0) /(max-min)) + "],";
			if( (i + 1) % 8 == 0 ){
				missedBits.add(s);
				s = "";
			}
		}
		File missedBitsTextFile = ReportUtils.createNewFile(dir, ReportUtils._MISSED_BITS_REPORT_FILE_NAME);
		ReportUtils.writeToFile(missedBitsTextFile, missedBits);
		
		BufferedImage img = new BufferedImage(8 * _STEP_SIZE, 10 * _STEP_SIZE, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < 8 * _STEP_SIZE; x ++){
			for(int y = 0; y < 10 * _STEP_SIZE; y ++){
				int yPos = (y / _STEP_SIZE);
				int xPos = (x / _STEP_SIZE);
				int pos = (yPos * 8) + xPos;
				int redComponent = (int) (( ((binaryInaccuracies[pos]-min) * 1.0) / (max-min)) * 255);
				img.setRGB(x, y, (new Color(redComponent, 0, 0)).getRGB());
			}
		}
		
	    try {
	    	File outputfile = ReportUtils.createNewFile(dir, ReportUtils._MISSED_BITS_REPORT_IMAGE_NAME);
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public void savePack(){
		PrintWriter writer;
		try {
			writer = new PrintWriter(_MESSAGES_SAVE_PATH, "UTF-8");
			for(String s : messages) writer.println(s);
			writer.close();
		}catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
	}
	
	public void flush(){
		messages = new String[_PACK_SIZE];
		binaryMessages = new String[_PACK_SIZE];
		binaryInaccuracies = new int[_MESSAGE_LENGTH_BITS];
		testedAccuracies = 0;
	}
	
	public void build(){
		for(int i = 0; i < _PACK_SIZE; i ++){
			messages[i] = generateMessage();
			binaryMessages[i] = convertStringToBinary(messages[i]);
		}
	}
	
	/**
	 * Calculates the accuracy from 0-100 of two binary strings.
	 * Will update the totals in binaryInaccuracies as it runs through the string.
	 * @param a Original String
	 * @param b Resultant String
	 * @return Accuracy b/w a and b [0,100]
	 */
	public double getAccuracy(String a, String b){
		testedAccuracies ++;
		if(a.length() == 0) return 0;
		double similarBits = 0;
		
		for(int i = 0; i < a.length(); i ++){
			if(b.length() - 1 < i) continue;
			if(a.charAt(i) == b.charAt(i)) similarBits ++;
			else binaryInaccuracies[i] ++;
		}
		
		return similarBits / a.length() * 100.00;
	}
	
	/**
	 * Converts a string to binary without leading zeros (7 bits/char), and with three extra zeros at the end.
	 * @param s
	 * @return
	 */
	public static String convertStringToBinary(String s){
		  byte[] bytes = s.getBytes();
		  String result = "";
		  for (byte b : bytes){
		     int val = b;
		     for (int i = 0; i < 8; i++){
		    	if(i == 0){
		    		val <<= 1;
		    		continue;
		    	}
		        result += ((val & 128) == 0 ? 0 : 1);
		        val <<= 1;
		     }
		  }
		  return result + "000";
	}
	
	private String generateMessage(){
		String s = "";
		for(int i = 0; i < _MESSAGE_LENGTH_CHARS; i ++){
			s += _ALLOWED_CHARACTERS.charAt(rand.nextInt(26)) + "";
		}
		return s;
	}
	
}
