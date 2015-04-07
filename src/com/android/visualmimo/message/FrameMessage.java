package com.android.visualmimo.message;

/**
 * Represents the message of a single MIMOFrame
 */
public class FrameMessage {
	/** The ASCII message */
	public String message;
	
	/** The embedded index */
	public int index;
	
	/** How many times this exact message has been received */
	public int count = 1;
	
	/**
	 * Gets message and index out of a pattern.
	 * @param pattern
	 * @throws Exception 
	 */
	public FrameMessage(boolean[] pattern) throws Exception {
		StringBuffer messageBuffer = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 8; i++) {
			for (int j = 9; j >= 0; j--) {
				boolean b = pattern[i * 10 + j];
				if(b)
					sb.append("0");
				else
					sb.append("1");
				if (sb.length() == 7) {
					messageBuffer.append((char)Integer.parseInt(sb.toString(), 2));
					sb = new StringBuffer();
				}
			}
		}
		
		if (sb.toString().charAt(sb.length() - 1) == '1') {
			throw new Exception("sync bit reports mismatch");
		}
		
		this.message = messageBuffer.toString();
		this.index = Integer.parseInt(sb.toString().substring(0, sb.length() - 1), 2);
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof FrameMessage && message.equals(((FrameMessage) o).message);
	}
}
