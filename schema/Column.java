package schema;

import schema.dbvars.Float;

public class Column {
	public final DBVar.Type type;
	public final String name;

	public Object min;
	public Object max;
	public float avg;
	public Object sum;
	public long count;

	public Column(DBVar.Type type, String name) {
		this.type = type;
		this.name = name;

		if (type == DBVar.Type.FLOAT) {
			min = Float.MAX_VALUE;
			max = Float.MIN_VALUE;
			sum = 0F;
		}
		if (type == DBVar.Type.INT) {
			min = Long.MAX_VALUE;
			max = Long.MIN_VALUE;
			sum = 0L;
		}
		if (type == DBVar.Type.TS) {
			min = -1L;
			max = 0L;
			sum = 0L;
		}
	}
}
