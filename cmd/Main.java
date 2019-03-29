package cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import commands.*;
import commands.Select;
import commands.Select.Expression;
import parsing.Parser;
import schema.Column;
import schema.VarType;

public class Main {
	public static String rootdir = "";
	private static boolean verbose;
	private static boolean useCommandLine = true;
	private static Scanner codeReader;

	public static void main(String[] args) {
		applyArgs(args);
		try {
			while (true) {


				String code = readCommand();

				if (code.equals("exit();") || code.equals("exit;"))
					System.exit(0);
				if (code.equals("test();") || code.equals("test;")) {
					test();
					System.exit(0);
				}

				Parser parser = new Parser(code);
				Command cmd = parser.parse();
				cmd.run();
			}
		} catch (Exception e) {
			System.err.println("ERROR:\n");
			System.out.println(e.getLocalizedMessage());

			e.printStackTrace();
		}
		codeReader.close();
	}

	private static void applyArgs(String[] args) {
		codeReader = null;
		try {
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
				case "--rootdir":
					rootdir = args[i + 1];
					new File(rootdir).mkdirs(); // result of mkdirs() is ignored - why?
					i++;
					break;
				case "--run":
					if (codeReader == null)
						throw new Exception();
					codeReader = new Scanner(new File(args[i + 1]));
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
	 */
	/*
	 * TODO:
	 * if the ';' is in quotes or escaped or before some other text it doesn't work
	 */
	private static String readCommand() {
		if (useCommandLine)
			System.out.print("csvdb>");
		StringBuilder code = new StringBuilder();
		while (!code.toString().endsWith(";")) {
			String line = codeReader.nextLine();
			int note = line.indexOf("--");
			if (note != -1)
				line = line.substring(0, note);
			code.append(line);
			if (line.endsWith(";"))
				break;
			code.append("\n\r"); // i don't want to add '\n\r' after ';'
		}
		return code.toString();
	}

	private static void test() {
		Column[] columns = {new Column(VarType.VARCHAR, "c1"), new Column(VarType.VARCHAR, "c2"),
				new Column(VarType.VARCHAR, "c3"), new Column(VarType.INT, "c4")};
		new Create("testtest", false, columns).run();
		new Load("C:\\Users\\flash_000\\Desktop\\testCsv.csv", "testtest", 0).run();
		Expression[] expressions = {new Expression("c1"), new Expression("c3", "c5"), new Expression("c4", "c")};
		new Select("aaa", "testtest", expressions, new Select.Condition("c", Select.Operator.big, 25L), null, null).run();
		System.out.println("end test");
	}
}
