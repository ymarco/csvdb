package schema;

public class Column {
	public final VarType type;
	public final String name;

	public Object min;
	public Object max;
	public float avg;
	public Object sum;
	public long count;

	public Column(VarType type, String name) {
		this.type = type;
		this.name = name;

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
}
