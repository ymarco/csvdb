package parsing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Token {
	public final Type type;
	public final String val; // yes, even ints and floats are held as strings here.

	Token(Type type, String val) {
		this.type = type;
		this.val = val;
	}

	public String toString() {
		if (type == Type.EOF)
			return "Token[EOF]";
		return "Token[" + type.name() + ", " + val + "]";
	}

	public boolean equals(Object other) {
		if (!(other instanceof Token))
			return false;
		Token otherToken = (Token) other;
		return otherToken.type == type && otherToken.val.equals(val);
	}

	public static final Set<String> keywords = new HashSet<>(
			Arrays.asList("and", "as", "asc", "avg", "by", "count", "create", "data", "desc", "drop", "exists", "float", "from",
					"group", "having", "if", "ignore", "infile", "int", "into", "lines", "load", "max", "min",
					"not", "null", "or", "order", "outfile", "select", "sum", "table", "timestamp", "varchar", "where"));

	public static final Set<String> operators = new HashSet<>(
			Arrays.asList("(", ")", "*", ",", "<", "<=", "<>", "=", ">", ">="));

	public enum Type {
		EOF,
		KEYWORD,
		IDENTIFIER,
		LIT_STR,
		LIT_NUM,
		OPERATOR
	}
}
