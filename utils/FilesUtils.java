package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
}
