package commandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Tester {
	private static void runTest(File testDir) {
		String[] args = {"--rootdir", testDir.getAbsolutePath(),
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

		return readAllFile(filename).replaceAll("\\s+", "");
	}

	public static void main(String[] args) throws IOException {
		String testDirName = args[0];
		File testsDir = new File(testDirName);
		if (!testsDir.isDirectory()) throw new RuntimeException("test dir is to a directory");
		for (File testDir : Arrays.stream(testsDir.listFiles()).filter(File::isDirectory).collect(Collectors.toList())) {
			runTest(testDir);
			String outname = testDir.getAbsolutePath() + File.separator + "output2.csv";
			String out = parseFile(outname);
			String goodname = testDir.getAbsolutePath() + File.separator + "good_output2.csv";
			String good = parseFile(goodname);
			System.out.println("aoeu");
			if (out.equals(good)) {
				System.out.println("passed test " + testDirName);
			} else {
				System.out.println("failed test " + testDirName);
				System.out.println("good:\n" + good);
				System.out.println("out:\n" + out);

			}
		}
	}


}
