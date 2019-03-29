package commands.select;

public enum Operator {
	lit,
	litEq,
	eq,
	bigEq,
	big,
	notEq;

	public static Operator Get(String op) {
		switch (op) {
		case "<":
			return lit;
		case "<=":
			return litEq;
		case "=":
			return eq;
		case ">=":
			return bigEq;
		case ">":
			return big;
		case "<>":
			return notEq;
		default:
			return null;
		}
	}
}
