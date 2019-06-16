package schema;

public class Column {
	public final DBVar.Type type;
	public final String name;

	public Object min;
	public Object max;
	public double avg;
	public Object sum;
	public long count;

	public Column(DBVar.Type type, String name) {
		this.type = type;
		this.name = name;

		switch (type) {

			case INT:
				min = Long.MAX_VALUE;
				max = Long.MIN_VALUE;
				sum = 0L;
				break;
			case FLOAT:
				min = Float.MAX_VALUE;
				max = Float.MIN_VALUE;
				sum = 0F;
				break;
			case VARCHAR:
				min = null;
				max = null;
				sum = null;
				break;
			case TS:
				min = -1L;
				max = 0L;
				sum = 0L;
				break;
		}
	}
}
