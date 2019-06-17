package commandLine;

import java.util.Scanner;

public class CharScanner {
	private Scanner scanner;
	private String line = "";
	private int indexAtLine;
	
	public CharScanner(Scanner scanner) {
		this.scanner = scanner;
	}
	
	public char next() {
		if (indexAtLine >= line.length()) {
			line = scanner.nextLine();
			indexAtLine = 0;
			return '\n';
		}
		return line.charAt(indexAtLine++);
	}
	
	public boolean hasNext() {
		return indexAtLine < line.length() || scanner.hasNextLine();
	}
	
	public void close() {
		scanner.close();
	}
}
