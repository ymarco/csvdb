package cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import commands.Create;
import commands.Drop;
import enums.VarType;
import schema.Column;

public class Main {
	public static String rootdir = "";
	public static boolean verbose;
	public static boolean cmd = true;
	public static Scanner codeReader;
	
	public static void main(String[] args) {
		readArgs(args);
		try {
			while (runCommand());
		}
		catch (Exception e) {
			System.err.println("ERROR:\n");
			System.out.println(e.getLocalizedMessage());
		}
		codeReader.close();
	}

	static void readArgs(String[] args) {
		codeReader = null;
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--rootdir")) {
					rootdir = args[i + 1];
					i++;
				}
				else if (args[i].equals("--run")) {
					if (codeReader == null)
						throw new Exception();
					codeReader = new Scanner(new File(args[i + 1]));
					cmd = false;
					i++;
				}
				else if (args[i].equals("--verbose")) {
					verbose = true;
				}
			}
			if (codeReader == null)
				codeReader = new Scanner(System.in);
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			System.exit(-1);
		} catch (Exception e) {
			System.out.println("Args aren't proper"); // proper is the good word?
			System.exit(-1);
		}
	}
	
	/**
	 * @return if continue to run
	 * 
	 * need to fix:
	 * if the ';' is in "" or before some other text he don't work
	 */
	static boolean runCommand() {
		if (cmd)
			System.out.print("csvdb>");
		String code = "";
		while (!code.endsWith(";")) {
			String line = codeReader.nextLine();
			int note = line.indexOf("--");
			if (note != -1)
				line = line.substring(0, note);
			code += line;
			if (line.endsWith(";"))
				break;
			code += " "; // i don't want to add ' ' after ';'
		}
		if (code.equals("exit();") || code.equals("exit;"))
			return false;
		if (code.equals("test;")) {
			test();
			return false;
		}
		parseAndRun(code);
		return cmd || codeReader.hasNext();
	}
	
	static void parseAndRun(String code) {
		
	}
	
	static void test() {
		Column[] columns = {new Column(VarType.INT, "te"), new Column(VarType.FLOAT, "st")};
		Create.run("testtest", false, columns);
		Drop.run("testtest", false);
//		Create.run("testtest", false, columns);
		Drop.run("testtest", true);
		System.out.println("end test");
	}
}
