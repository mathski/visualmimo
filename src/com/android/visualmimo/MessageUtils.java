package com.android.visualmimo;

import java.io.PrintStream;

public class MessageUtils {
	/**
	 * Prints a grid representation of the pattern.
	 */
	public static void printGrid(boolean[] pattern, PrintStream out) {
		out.println("MESSAGE:");
		for (int i = 0; i < 8; i++) {
			for (int j = 9; j >= 0; j--) {
				boolean b = pattern[i * 10 + j];
				if (b)
					out.print(". ");
				else
					out.print("X ");
			}
			out.println();
		}
	}
	
	/**
	 * Prints a 1D array representation of the pattern.
	 */
	public static void printArray(boolean[] pattern, PrintStream out) {
		out.print("MESSAGE MATLAB: [");
		for (int i = 0; i < 8; i++) {
			for (int j = 9; j >= 0; j--) {
				boolean b = pattern[i * 10 + j];
				if (b)
					out.print("0, ");
				else
					out.print("1, ");
			}
		}
		out.println("];");
	}
	
	/**
	 * Extracts an ASCII message from the pattern.
	 * @param message
	 * @return
	 */
	public static String parseMessage(boolean[] pattern) {
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
		
		return messageBuffer.toString();
	}
	
	public static double checkAccuracy(boolean[] pattern) {
		
		//reference is "abcdefghijk"
//		boolean[] reference = {false, false, true, true, true, true, false, false, false, true, true, true, false, true, false, false, true, true, true, false, false, false, false, true, true, false, true, true, false, false, true, true, false, true, false, false, false, true, true, false, false, true, false, false, true, true, false, false, false, false, false, true, false, true, true, true, false, false, true, false, true, true, false, false, false, true, false, true, false, true, false, false, true, false, true, false, false, true, true, true};
		
		//reference is "abcdefghijklmnopqrstuv"
		boolean[] reference = {false, false, true, true, true, true, false, false, false, true, true, true, false, true, false, false, true, true, true, false, false, false, false, true, true, false, true, true, false, false, true, true, false, true, false, false, false, true, true, false, false, true, false, false, true, true, false, false, false, false, false, true, false, true, true, true, false, false, true, false, true, true, false, false, false, true, false, true, false, true, false, false, true, false, true, false, false, false, false, true, false, false, true, true, false, false, true, false, false, true, false, false, false, true, false, false, false, true, false, false, true, false, false, false, false, false, false, false, true, true, true, true, false, false, false, true, true, true, false, false, false, false, true, true, false, true, false, false, false, true, true, false, false, false, false, false, true, false, true, true, false, false, false, true, false, true, false, false, false, false, true, false, false, true, true, true, true, true, true, true};

		double correct = 0;
		int pos = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 9; j >= 0; j--) {
				if (pattern[i * 10 + j] == reference[pos++]) {
					correct++;
				}
			}
		}
		
		return correct / reference.length;
	}
	
	public static void invertPattern(boolean[] pattern) {
		for (int i = 0; i < pattern.length; i++) {
			pattern[i] = !pattern[i];
		}
	}
}
