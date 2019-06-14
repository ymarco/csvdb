package commandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import commands.Command;
import parsing.Parser;

public class Main {
	public static String rootdir = ".";
	public static String columnFilesExtensios = ".col";
	private static boolean verbose;
	private static boolean useCommandLine = true;
	private static Scanner codeReader = null;

	public static void main(String[] args) {
		parseArgs(args);
		LoadData.load();

		while (true) {
			try {

				String code = null;
				try {
					code = readCommand();
				} catch (NoSuchElementException e) {
					System.exit(0);
				}

				if (code.equals("exit();") || code.equals("exit;"))
					break;
				if (code.equals("pred();") || code.equals("pred;")) {
					test();
					break;
				}

				Parser parser = new Parser(code);
				Command cmd = parser.parse();
				cmd.run();
			} catch (Exception e) {
				System.out.println("ERROR:\n");
				System.out.println(e.getLocalizedMessage());
				if (verbose)
					e.printStackTrace();
				System.err.flush();
				if (!useCommandLine)
					break;
			}
		}

		codeReader.close();
	}

	private static void parseArgs(String[] args) {
		try {
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
					case "--rootdir":
						rootdir = args[i + 1];
						File rootdirFile = new File(rootdir);
						rootdirFile.mkdirs(); // return if the file created, we need to know if the file are exists.
						if (!rootdirFile.exists()) {
							System.out.println("failed to load the rootdir");
							System.exit(-1);
						}
						i++;
						break;
					case "--run":
						if (codeReader == null)
							throw new Exception();
						codeReader = new Scanner(new File(args[i + 1])).useDelimiter(";");
						useCommandLine = false;
						i++;
						break;
					case "--verbose":
						verbose = true;
						break;
					default:
						System.out.println("Usage: " +
								"csvdb [--verbose] [--run file] [--rootdir dir]");
						System.exit(1);

				}
			}
			if (codeReader == null)
				codeReader = new Scanner(System.in).useDelimiter(";");

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
	 */
	/*
	 * TODO:
	 * if the ';' is in quotes or escaped it doesn't work
	 */
	private static String readCommand() throws NoSuchElementException {
		if (useCommandLine)
			System.out.print("csvdb>");
		String code = codeReader.next() + ";";
		code.replaceAll("--.*$", "");
		while (!code.endsWith(";")) {
			code += codeReader.next() + ";";
			code.replaceAll("--.*$", "");
		}
		while (code.startsWith("\r") || code.startsWith("\n"))
			code = code.substring(2);
		return code;
	}

	private static void test() {

	}
}
