package com.android.visualmimo.message;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MIMOMessage {
	public Map<Integer, ConcurrentHashMap<String, FrameMessage>> frames
		= new ConcurrentHashMap<Integer, ConcurrentHashMap<String, FrameMessage>>();
	
	public MIMOMessage () {
		
	}
	
	/**
	 * Constructs FrameMessage from pattern and either adds to frames or
	 * increments existing entry if messages agree.
	 * @param pattern
	 */
	public void addPattern(boolean[] pattern) {
		try {
			FrameMessage m = new FrameMessage(pattern);
			Map<String, FrameMessage> hmAtIndex = frames.get(m.index);
			if (hmAtIndex != null) {
				FrameMessage match = hmAtIndex.get(m.message);
				if (match != null) {
					match.count++;
				} else {
					hmAtIndex.put(m.message, m);
				}
			} else {
				frames.put(m.index, new ConcurrentHashMap<String, FrameMessage>());
				frames.get(m.index).put(m.message, m);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets best message from frames.
	 */
	public String getMessage() {
		StringBuffer sb = new StringBuffer();
		
		ArrayList<Integer> indices = new ArrayList<Integer>(frames.keySet());
		java.util.Collections.sort(indices);
		
		for (Integer index : indices) {
			FrameMessage best = null;
			
			System.out.println("index: " + index.intValue());
			
			for (FrameMessage mess : frames.get(index).values()) {
				System.out.println("\t " + mess.message + " : " + mess.count);
				if (best == null || best.count < mess.count) {
					best = mess;
				}
			}
			System.out.println("best:\t" + best.message);
			sb.append(best.message);
		}
		
		return sb.toString();
	}
}