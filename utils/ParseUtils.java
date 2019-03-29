package utils;

public class ParseUtils {
	public static boolean isAlphaOrUnderscore(char c) {
		return Character.isLetter(c) || c == '_';
	}

	public static boolean isDigitOrDotOrPM(char c) {
		return Character.isDigit(c) || c == '.' || c == '+' || c == '-';
	}

}
