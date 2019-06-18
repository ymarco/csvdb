package utils;

import java.io.*;

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
		for (String s : file.list()) {
			File currentFile = new File(file.getPath(), s);
			if (currentFile.isDirectory())
				clearFolder(currentFile);
			currentFile.delete();
		}
	}

	public static int countLines(String filename) throws IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
			byte[] c = new byte[1024];

			int readChars = is.read(c);
			if (readChars == -1) {
				// bail out if nothing to read
				return 0;
			}

			// make it easy for the optimizer to tune this loop
			int count = 0;
			while (readChars == 1024) {
				for (int i = 0; i < 1024; ) {
					if (c[i++] == '\n') {
						++count;
					}
				}
				readChars = is.read(c);
			}

			// count remaining characters
			while (readChars != -1) {
				System.out.println(readChars);
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
				readChars = is.read(c);
			}

			return count == 0 ? 1 : count;
		}
	}
}
