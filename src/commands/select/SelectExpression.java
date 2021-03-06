package commands.select;

/*
 this class represents one item of a select expression
 example:
 - year
 - max(rating)
 - title as coteret
*/
public class SelectExpression {

	public String fieldName;
	public String asName;
	public Aggregator agg;

	public SelectExpression(String fieldName) {
		this(fieldName, new Aggregator.EmptyAgg());
	}

	public SelectExpression(String fieldName, Aggregator agg) {
		this(fieldName, fieldName, agg);
	}

	public SelectExpression(String fieldName, String asName) {
		this(fieldName, asName, new Aggregator.EmptyAgg());
	}


	public SelectExpression(String fieldName, String asName, Aggregator agg) {
		this.fieldName = fieldName;
		this.asName = asName;
		this.agg = agg;
	}

	@Override
	public String toString() {
		return "SelectExpression{" +
				"fieldName='" + fieldName + '\'' +
				", asName='" + asName + '\'' +
				", agg=" + agg +
				'}';
	}
}
