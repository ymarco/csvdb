package commandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Tester {
	private static void runTest(File testDir) {
		String[] args = {"--verbose", "--rootdir", testDir.getAbsolutePath(),
				"--run", testDir.getAbsolutePath() + File.separator + "test.sql"};
		Main.main(args);
	}


	private static String readAllFile(String filename) throws IOException {
		File file = new File(filename);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		return new String(data, StandardCharsets.UTF_8);
	}

	private static String parseFile(String filename) throws IOException {

		return readAllFile(filename).replaceAll(" ", "").trim();
	}

	public static void main(String[] args) throws IOException {
		String testDirName = args[0];
		File testDir = new File(testDirName);
		assert testDir.isDirectory() : "test dir is to a directory";
		runTest(testDir);
		List<String> outs = new ArrayList<>();
		List<String> goods = new ArrayList<>();
		for (File f : testDir.listFiles()) {
			if (f.getName().startsWith("output"))
				outs.add(f.getAbsolutePath());
			else if (f.getName().startsWith("good_output"))
				goods.add(f.getAbsolutePath());
		}
		outs.sort(String::compareTo);
		goods.sort(String::compareTo);
		assert outs.size() == goods.size() : "amount of good outputs doesnt match amount of outputs";
		for (int i = 0; i < outs.size(); i++) {
			System.out.println("testing " + testDirName);
			boolean passed = testOutput(outs.get(i), goods.get(i));
		}
	}

	private static boolean testOutput(String outname, String goodname) throws IOException {
		String out = parseFile(outname);
		String good = parseFile(goodname);
		boolean res = out.equals(good);
		if (res) {
			System.out.println("---passed  " + (new File(outname)).getName() + "," + (new File(goodname)).getName());
		} else {
			System.out.println("---failed  " + (new File(outname)).getName() + "," + (new File(goodname)).getName());
			System.out.println("---good:\n" + good);
			System.out.println("---out:\n" + out);
		}
		return res;
	}


}
