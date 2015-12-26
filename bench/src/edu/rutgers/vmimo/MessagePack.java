package edu.rutgers.vmimo;

import java.util.Random;

public class MessagePack {

	public static final int _PACK_SIZE = 2;
	public static final int _MESSAGE_LENGTH_CHARS = 11;
	public static final int _MESSAGE_LENGTH_BITS = 80;
	public Random rand = new Random();
	public String[] messages = new String[_PACK_SIZE];
	public String[] binaryMessages = new String[_PACK_SIZE];
	private static final String _ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
	
	public MessagePack(){
		build();
	}
	
	public MessagePack(String path){
		
	}

	public void flush(){
		messages = new String[_PACK_SIZE];
		binaryMessages = new String[_PACK_SIZE];
	}
	
	public void build(){
		for(int i = 0; i < _PACK_SIZE; i ++){
			messages[i] = generateMessage();
			binaryMessages[i] = convertStringToBinary(messages[i]);
		}
	}
	
	/**
	 * Calculates the accuracy from 0-100 of two strings
	 * @param a Original String
	 * @param b Resultant String
	 * @return Accuracy b/w a and b [0,100]
	 */
	public static double getAccuracy(String a, String b){
		if(a.length() == 0) return 0;
		double similarBits = 0;
		
		for(int i = 0; i < a.length(); i ++){
			if(b.length() - 1 < i) continue;
			if(a.charAt(i) == b.charAt(i)) similarBits ++;
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
		     for (int i = 1; i < 8; i++){
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
		System.out.println(s);
		return s;
	}
	
}
