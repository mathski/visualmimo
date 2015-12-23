package edu.rutgers.vmimo;

import java.util.Random;

public class MessagePack {

	public static final int _PACK_SIZE = 10;
	public static final int _MESSAGE_LENGTH = 80;
	public Random rand = new Random();
	public String[] messages = new String[_PACK_SIZE];
	
	
	public MessagePack(){
		build();
	}
	
	public MessagePack(String path){
		
	}

	public void flush(){
		messages = new String[_PACK_SIZE];
	}
	
	public void build(){
		for(int i = 0; i < _PACK_SIZE; i ++) messages[i] = generateMessage();
	}
	
	private String generateMessage(){
		String s = "";
		for(int i = 0; i < _MESSAGE_LENGTH; i ++){
			s += rand.nextInt(2) + "";
		}
		return s;
	}
	
}
