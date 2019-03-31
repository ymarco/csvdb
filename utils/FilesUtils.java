package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class FilesUtils {
	
	public static void closeAll(BufferedWriter[] files) throws IOException {
		for (BufferedWriter bw : files)
			if (bw != null)
				bw.close();
	}

	public static void closeAll(DataOutputStream[] files) throws IOException {
		for (DataOutputStream dos : files)
			if (dos != null)
				dos.close();
	}

	public static void closeAll(BufferedReader[] files) throws IOException {
		for (BufferedReader br : files)
			if (br != null)
				br.close();
	}

	public static void closeAll(DataInputStream[] files) throws IOException {
		for (DataInputStream dis : files)
			if (dis != null)
				dis.close();
	}
	
	public static String endoceStringForWriting(String s) {
		s = s.replace("\\", "\\\\");
		s = s.replace("\r\n", "\\n");
		return s;
	}
	
	public static String decodeStringFromWriting(String s) {
		s = s.replace("\\n", "\r\n");
		s = s.replace("\\\\", "\\");
		return s;
	}
	
	public static void clearFolder(File file) {
		for(String s: file.list()){
		    File currentFile = new File(file.getPath(),s);
		    if (currentFile.isDirectory())
		    	clearFolder(currentFile);
		    currentFile.delete();
		}
	}
}
