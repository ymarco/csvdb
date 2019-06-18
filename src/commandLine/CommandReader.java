package commandLine;

public class CommandReader {
	public static CharScanner scanner;

	public static String readCommand() {
		StringBuilder code = new StringBuilder();
		char c;
		char preC = ' ';
		do {
			c = scanner.next();

			//Notes
			if (c == '-') {
				if (preC == '-') {
					skipComment();
					preC = '\n';
					code.append('\n');
				} else {
					preC = '-';
				}
				continue;
			} else if (preC == '-') {
				code.append('-');
			}


			if (c == '"') //String
				code.append(readString());
			else //Other options (';' or a unspecial char)
				code.append(c);
			//Update preC
			preC = c;
		} while (c != ';');
		//if ... (i don't know to say this in English)
		return code.toString();
	}

	private static StringBuilder readString() {
		StringBuilder sb = new StringBuilder("\"");
		char c;
		do {
			c = scanner.next();
			sb.append(c);
		} while (c != '"');
		return sb;
	}

	private static void skipComment() {
		while (scanner.next() != '\n') ;
	}
}
