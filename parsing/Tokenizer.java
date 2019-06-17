package parsing;

import utils.TextUtils;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;


class Tokenizer {
	private final String text;
	private final String[] text_split_by_lines;
	private int curser = 0; // index to text
	/* these are used for error printing */
	private int row = 0;
	private int column = 0;

	Tokenizer(String text) {
//		this.text = text.toLowerCase();
		this.text = text;
		this.text_split_by_lines = text.split("\n");
	}

	/**
	 * @return next pair of type-value Token when reaching EOF, the EOF token is
	 * returned repeatedly.
	 */
	Token nextToken() {
		skip();
		if (eof() || cur() == ';')
			return new Token(Token.Type.EOF, "");
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
		return curser >= text.length(); //you did return curser < text.length();
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
		if ((curser + 1) >= text.length()) {
			/*
			 * there is 1 char left before eof, so there is no chance for a comment
			 */
			return;
		}
		if (text.substring(curser, curser + 2).equals("--")) {
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
		res += "\n" + TextUtils.repert(" ", column) + "^^^";
		return res;
	}

	void throwErr(String msg) {
		throw new TokenizingException(msg);
	}

	private Token getIdentifierOrKeyword() {
		StringBuilder token_val = new StringBuilder();
		while (!eof() && (TextUtils.isAlphaOrUnderscore(cur()) || Character.isDigit(cur()))) {
			token_val.append(cur());
			proceedCur();
		}
		String token_val_string = token_val.toString().toLowerCase();
		if (Token.keywords.contains(token_val_string))
			return new Token(Token.Type.KEYWORD, token_val_string);
		return new Token(Token.Type.IDENTIFIER, token_val_string);
	}

	private Token getLitNum() throws TokenizingException {// note that this returns a STRING containing the num, e.g. "34" and NOT 34
		// int start_pos = curser; //used for error printing
		StringBuilder token_val = new StringBuilder();
		if (!TextUtils.isDigitOrDotOrPM(cur()))
			throw new TokenizingException("Tokenizer: _get_lit_num was called, but the 'number' didnt start with digit,.,+,- in the curser");
		boolean is_dotted = false;
		while (!eof()) {
			if (Character.isDigit(cur())) {
				token_val.append(cur());
			} else if (cur() == '.') {
				if (is_dotted) {
					throw new TokenizingException("Tokenizer: _get_lit_num found 2 dots without exponent");
				} else {
					is_dotted = true;
					token_val.append(cur());
				}
			} else if (Token.operators.contains("" + cur()) || cur() == ';') { // end of number
				return new Token(Token.Type.LIT_NUM, token_val.toString());
			} else {
				throw new TokenizingException("Tokenizer: a thing started with a number and than changed into somethng else invalid");
			}
			proceedCur();
		}
		/*
		 * if we got here it means we reached eof and the numbe has ended
		 */
		return new Token(Token.Type.LIT_NUM, token_val.toString());
	}

	private Token getLitStr() {
		StringBuilder token_val = new StringBuilder();
		if (cur() != '"')
			throw new TokenizingException("Tokenizer: _get_lit_str was called, but there wasnt a \" in the _cur");
		proceedCur(); // now we are inside the str lit
		while (!eof()) {
			if (cur() == '"') { // found the closing "
				proceedCur(); // going past the " and exiting the str lit
				return new Token(Token.Type.LIT_STR, token_val.toString());
			}
			token_val.append(cur());
			proceedCur();
		}
		/*
		 * if we got here it means that we reached eof without finding the closing "
		 */
		throw new TokenizingException("Tokenizer: did not find the closing \"");
	}

	private Token getOperator() {
		Optional<String> maybeOperator = Token.operators.stream()
				.map(op -> TextUtils.getStartWith(op, text.substring(curser)))
				.filter(Objects::nonNull).max(Comparator.comparingInt(String::length));
		if (!maybeOperator.isPresent())
			throw new TokenizingException("Syntax Error: invalid token");

		String op = maybeOperator.get();
		for (int i = 0; i < op.length(); i++) proceedCur();
		return new Token(Token.Type.OPERATOR, op);
	}

	public class TokenizingException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		TokenizingException(String message) {
			super(errInfo() + "\n" + message);
		}
	}
}
