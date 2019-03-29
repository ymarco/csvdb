package tokenizer;

import utils.TextUtils;


public class Tokenizer {
	private final String text;
	private final String[] text_split_by_lines;
	private int curser = 0; // index to text
	/* these are used for error printing */
	private int row = 0;
	private int column = 0;
	
	public Tokenizer(String text) {
		this.text = text;
		this.text_split_by_lines = text.split("\n");
	}
	
	/**
	 * @return next pair of type-value Token when reaching EOF, the EOF token is
	 *         returned repeatedly.
	 */
	public Token nextToken() {
		skip();
		if (eof())
			return new Token(TokenType.EOF, "");
		if (TextUtils.isAlphaOrUnderscore(cur()))
			return getIdentifierOrKeyword();
		if (cur() == '"')
			return getLitStr();
		if (TextUtils.isDigitOrDotOrPM(cur()))
			return getLitNum();

		/*
		 * if we got here, it means that our current token is not an end-of-file,
		 * keyword, identifier, or literal so it must be an operator (or an error)
		 */
		return getOperator();
	}

	private boolean eof() {
		return curser < text.length();
	}

	private char cur() {
		return text.charAt(curser);
	}

	private void proceedCur() {
		if (cur() == '\n') {
			row++;
			column = 0;
		} else {
			column++;
		}
		curser++;
	}

	private void skip() {
		skipWspace();
		skipComment();
		skipWspace();
	}

	private void skipWspace() {
		while (!eof() && TextUtils.isSpace(cur()))
			proceedCur();
	}

	private void skipComment() {
		if ((curser + 1) == text.length()) {
			/*
			 * there is 1 char left before eof, so there is no chance for a comment
			 */
			return;
		}
		if (text.substring(curser, 2).equals("--")) {
			skipToNextLine();
		}
	}

	private void skipToNextLine() {
		while (cur() != '\n')
			proceedCur();
		/* now we are on the \n char */
		proceedCur();
	}

	private String errInfo() {
		String res = "";
		res += text_split_by_lines[row];
		/* We need a string multiplication thing here */
		res += "\n" + TextUtils.repert(" ", column) + "^^^" + "\n";
		return res;
	}

	void throwErr(String msg) {
		String info_and_msg = errInfo() + msg + "\n";
		throw new RuntimeException(info_and_msg);
	}

	private Token getIdentifierOrKeyword() {
		String token_val = "";
		while (!eof() && (TextUtils.isAlphaOrUnderscore(cur()) || Character.isDigit(cur()))) {
			token_val += cur();
			proceedCur();
		}
		String token_val_lower = token_val.toLowerCase();
		if (Token.keywords.contains(token_val_lower))
			return new Token(TokenType.KEYWORD, token_val_lower);
		return new Token(TokenType.IDENTIFIER, token_val);
	}

	private Token getLitNum() {// note that this returns a STRING containing the num, e.g. "34" and NOT 34
		// int start_pos = curser; //used for error printing
		String token_val = "";
		if (!TextUtils.isDigitOrDotOrPM(cur()))
			throwErr("Tokenizer: _get_lit_num was called, but the \"number\" didnt start with digit,.,+,- in the curser");
		boolean is_dotted = false;
		while (!eof()) {
			if (Character.isDigit(cur())) {
				token_val += cur();
			} else if (cur() == '.') {
				if (is_dotted) {
					throwErr("Tokenizer: _get_lit_num found 2 dots without exponent");
				} else {
					is_dotted = true;
					token_val += cur();
				}
			} else if (TextUtils.isSpace(cur())) { // end of number
				return new Token(TokenType.LIT_NUM, token_val);
			} else {
				throwErr("Tokenizer: a thing started with a number and than changed into somethng else invalid");
			}
			proceedCur();
		}
		/*
		 * if we got here it means we reached eof and the numbe has ended
		 */
		return new Token(TokenType.LIT_NUM, token_val);
	}

	private Token getLitStr() {
		String token_val = "";
		if (cur() != '"')
			throwErr("Tokenizer: _get_lit_str was called, but there wasnt a \" in the _cur");
		proceedCur(); // now we are inside the str lit
		while (!eof()) {
			if (cur() == '"') { // found the closing "
				proceedCur(); // going past the " and exiting the str lit
				return new Token(TokenType.LIT_STR, token_val);
			}
			token_val += cur();
			proceedCur();
		}
		/*
		 * if we got here it means that we reached eof without finding the closing "
		 */
		throwErr("Tokenizer: did not find the closing \"");
		/* unreachable */
		return null;
	}

	private Token getOperator() {
		String token_val = "";
		token_val += cur();
		proceedCur();
		if (eof()) {
			if (Token.operators.contains(token_val))
				return new Token(TokenType.KEYWORD, token_val);
			else
				throwErr("Syntax Error: invalid token");
		}
		/*
		 * if we got here it means that it can be a 2-char operator (or an error)
		 */
		token_val += cur();
		proceedCur();
		if (Token.operators.contains(token_val))
			return new Token(TokenType.KEYWORD, token_val);
		else
			throwErr("Syntax Error: invalid token");
		/* unreachable */
		return null;
	}
}
