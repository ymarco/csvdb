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
}
