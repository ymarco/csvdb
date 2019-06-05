package schema;

public class Column2 {
	public final VarType type;
	public final String name;

	public Object min;
	public Object max;
	public float avg;
	public Object sum;
	public long count;
	public String filePath;

	public Column2(VarType type, String name, String filePath) {
		this.type = type;
		this.name = name;
		this.filePath = filePath;
		
		if (type == VarType.FLOAT) {
			min = Float.MAX_VALUE;
			max = Float.MIN_VALUE;
			sum = 0F;
		}
		if (type == VarType.INT) {
			min = Long.MAX_VALUE;
			max = Long.MIN_VALUE;
			sum = 0L;
		}
		if (type == VarType.TIMESTAMP) {
			min = -1L;
			max = 0L;
			sum = 0L;
		}
		
		
	}
	
	//TODO add aggrate.
	
	
	
	long[] valuesI;
	double[] valuesF;
	long[] valuesTS;
	String[] valuesV;
	
	
	public void loadToMemoryI() {
		
	}
	
}
