package schema;

public class DBVar {
	public long i;
	public String s;
	public double f;
	public long ts; // ts is treated as an UNSIGNED long
	public VarType varType;
	
	public static final long   NULL_INT = Long.MIN_VALUE;
	public static final String NULL_STRING = "";
	public static final double NULL_FLOAT = Double.NEGATIVE_INFINITY;
	public static final long   NULL_TS = 0;
}
