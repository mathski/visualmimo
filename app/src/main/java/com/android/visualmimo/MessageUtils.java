package com.android.visualmimo;

import java.io.PrintStream;

public class MessageUtils {
	/**
	 * Prints a grid representation of the pattern.
	 */
	public static void printGrid(boolean[] pattern, PrintStream out) {
		out.println("MESSAGE:");
		int k = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 10; j++) {
				if ((i == 0 && (j == 0 || j == 9)) || (i == 7 && (j == 0 || j == 9))) {
					out.print("S ");
				} else {
					out.print(pattern[k++] ? "X " : ". ");
				}
			}
			out.println();
		}
	}
	
	/**
	 * Prints a 1D array representation of the pattern.
	 */
	public static void printArray(boolean[] pattern, PrintStream out) {
		out.print("MESSAGE MATLAB: [");
		for (boolean b : pattern) {
			out.print(b ? "1, " : "0, ");
		}
		out.println("];");
	}
	
	/**
	 * Extracts an ASCII message from the pattern.
	 * @param pattern
	 * @return
	 */
	public static String parseMessage(boolean[] pattern) {
		StringBuffer messageBuffer = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		for (boolean b : pattern) {
			sb.append(b ? "1" : "0");
			if (sb.length() == 7) {
				messageBuffer.append((char) Integer.parseInt(sb.toString(), 2));
				sb = new StringBuffer();
			}
		}

		return messageBuffer.toString();
	}

	/**
	 * Extracts binary string from the pattern.
	 * @param pattern
	 * @return
	 */
	public static String parseMessageToBinary(boolean[] pattern) {
		StringBuffer sb = new StringBuffer();
		for (boolean b : pattern) {
			sb.append(b ? "1" : "0");
		}

		return sb.toString();
	}
	
	public static double checkAccuracy(boolean[] pattern) {
		
		//reference is "abcdefghij"
		boolean[] reference = {true,true,false,false,false,false,true,true,true,false,false,false,true,false,true,true,false,false,false,true,true,true,true,false,false,true,false,false,true,true,false,false,true,false,true,true,true,false,false,true,true,false,true,true,false,false,true,true,true,true,true,false,true,false,false,false,true,true,false,true,false,false,true,true,true,false,true,false,true,false,false,false,false,false,false,false};

		double correct = 0;
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] == reference[i]) {
				correct++;
			}
		}
		
		return correct / pattern.length;
	}
	
	public static void invertPattern(boolean[] pattern) {
		for (int i = 0; i < pattern.length; i++) {
			pattern[i] = !pattern[i];
		}
	}
}
