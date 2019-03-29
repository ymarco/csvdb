package schema;

public enum VarType {
	INT("int"),
    FLOAT("float"),
    VARCHAR("varchar"),
    TIMESTAMP("timestamp");
	
	String toString;
	
	private VarType(String toString) {
		this.toString = toString;
	}
	
	public String toString() {
		return toString; 
	}
	
	public static VarType toVarType(String s) {
		switch (s.toLowerCase()) {
		case "int": return INT;
		case "varchar": return VARCHAR;
		case "float": return FLOAT;
		case "timestamp": return TIMESTAMP;
		}
		return null;
	}
}
