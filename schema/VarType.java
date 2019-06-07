package schema;

import java.util.Comparator;

public enum VarType {
	INT("int",(x,y)->Long.compare(x.i, y.i)),
    FLOAT("float",(x,y)->Double.compare(x.f, y.f)),
    VARCHAR("varchar",(x,y)->Long.compareUnsigned(x.ts, y.ts)),
    TIMESTAMP("timestamp",(x,y)->x.s.compareTo(y.s));
	
	private final String toString;
	private final Comparator<DBVar> comparator;
	
	private VarType(String toString, Comparator<DBVar> comparator) {
		this.toString = toString;
		this.comparator = comparator;
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
	
	public Comparator<DBVar> getComparator() {
		return comparator;
	}
}
