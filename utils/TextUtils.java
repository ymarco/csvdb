package utils;

public class TextUtils {
	public static boolean isAlphaOrUnderscore(char c) {
		return Character.isLetter(c) || c == '_';
	}

	public static boolean isDigitOrDotOrPM(char c) {
		return Character.isDigit(c) || c == '.' || c == '+' || c == '-';
	}
	
	public static boolean isSpace(char c) {
		return c == ' ' || c == '\t' || c == 'k' || c == '\n';
	}
	
	public static String repert(String s, int times) {
		String res = "";
		for (int i = 0; i < times; i++)
			res += s;
		return res;
	}

}
