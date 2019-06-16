package commandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
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
		System.out.println(String.join(" ", args));
		parseArgs(args);
		LoadData.load();

		while (true) {
			try {

				String code = null;
				try {
					code = readCommand();
				} catch (NoSuchElementException e) {
					return;
				}

				// some hacky and quick commands
				if (code.equals("exit();") || code.equals("exit;"))
					break;
				if (code.equals("pred();") || code.equals("pred;")) {
					test();
					break;
				}

				// standard route
				System.out.println(code);
				Parser parser = new Parser(code);
				Command cmd = parser.parse();
				cmd.run();
			} catch (Exception e) {
				System.out.println("ERROR:\n");
				System.out.println(e.getLocalizedMessage());
				//if (verbose)
				e.printStackTrace();
				System.err.flush();
				if (!useCommandLine)
					break;
			}
		}

		codeReader.close();
	}

	private static void parseArgs(String[] args) {
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
					String filename = args[i + 1];
					try {
						codeReader = new Scanner(new File(filename)).useDelimiter(";");
					} catch (FileNotFoundException e) {
						throw new RuntimeException("--run: file " + filename + " not found");
					}
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

	}

	/**
	 * @return if continue to run
	 */
	/*
	 * TODO:
	 * if the ';' is in quotes or escaped it doesn't work
	 */
	private static String readCommand() throws NoSuchElementException {
		if (useCommandLine) {
			System.out.print("csvdb>");
		}
		StringBuilder code = new StringBuilder();
		do {
			code.append(codeReader.next()).append(";");
			code = new StringBuilder(code.toString().replaceAll("--.*$", ""));
		} while (!code.toString().endsWith(";"));
		return code.toString().trim();
	}

	private static void test() {

	}
}
