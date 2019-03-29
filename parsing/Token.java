package parsing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Token {
	public final TokenType type;
	public final String val; // yes, even ints and floats are held as strings here.

	Token(TokenType type, String val) {
		this.type = type;
		this.val = val;
	}

	public String toString() {
		if (type == TokenType.EOF)
			return "Token[EOF]";
		return "Token[" + type.name() + ", " + val + "]";
	}

	public static final Set<String> keywords = new HashSet<>(
			Arrays.asList("select", "from", "where", "avg", "sum", "min", "max", "load", "drop", "order", "by", "group",
					"into", "outfile", "as", "having", "data", "infile", "table", "ignore", "lines", "null", "int",
					"float", "varchar", "timestamp", "desc", "asc", "and", "or", "not", "create", "if", "exists"));

	public static final Set<String> operators = new HashSet<>(
			Arrays.asList(",", "(", ")", "<", "<=", "<>", "=", ">=", ">", ";", "*"));
}
