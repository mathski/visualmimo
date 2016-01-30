package edu.rutgers.vmimo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;

public class ReportUtils {

	public static final String _REPORTS_PATH = "reports";
	public static final String _REPORT_FOLDER_NAME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
	public static final String _REPORT_FILE_NAME = "report.txt";
	public static final String _MISSED_BITS_REPORT_FILE_NAME = "missedBits.txt";
	public static final String _MISSED_BITS_REPORT_IMAGE_NAME = "missedBits.png";
	
	public static File generateNewReportFolder(){
		SimpleDateFormat dateFormat = new SimpleDateFormat(_REPORT_FOLDER_NAME_FORMAT);
		File f = new File(_REPORTS_PATH + "/" + dateFormat.format(new Date()));
		f.mkdirs();
		return f;
	}

	public static void copyMessagePackToReports(File dir, MessagePack pack){
		try {
			FileUtils.copyFile(new File(pack._MESSAGES_SAVE_PATH), new File(dir, "messages.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new file in a directory
	 * @param dir Directory to create in.
	 * @param name Name of file to create.
	 * @return The resultant file.
	 */
	public static File createNewFile(File dir, String name){
		File f = new File(dir.getPath() + "/" + name);
		try {
			f.createNewFile();
			return f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeToFile(File file, ArrayList<String> lines){
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			for(String s : lines) writer.println(s);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
}
