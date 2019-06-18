package commands.select;

import schema.DBVar;
import schema.Schema;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GroupBy implements Statement, Iterator<DBVar[]> {
	private int[] colmnnToGroupBy;
	private Where having;
	public Schema schema;
	private SelectExpression[] expressions;
	private Aggregator[] aggs;
	private Iterator<DBVar[]> it;
	private DBVar[] key = null;
	private int[] selectedCols;

	private DBVar[] getKey(DBVar[] row) {
		DBVar[] res = new DBVar[colmnnToGroupBy.length];
		for (int i = 0; i < colmnnToGroupBy.length; i++) {
			res[i] = row[colmnnToGroupBy[i]];
		}
		return res;
	}

	public GroupBy(String tableName, int[] colmnnToGroupBy,
	               SelectExpression[] expressions, Where having) {
		this.colmnnToGroupBy = colmnnToGroupBy;
		this.having = having;
		this.expressions = expressions;
		this.schema = Schema.GetSchema(tableName);

		aggs = Arrays.stream(expressions).map(e -> e.agg).toArray(Aggregator[]::new);
		selectedCols = Arrays.stream(expressions).map(e -> e.fieldName).mapToInt(schema::getColumnIndex).toArray();
	}

	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
		OrderBy order = new OrderBy(colmnnToGroupBy);
		Stream<DBVar[]> originalOrdered = order.apply(s);
		it = originalOrdered.iterator();
		DBVar[] row = it.next();
		key = getKey(row);
		aggregateRow(row);
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
				false
		);
	}


	@Override
	public boolean hasNext() {
		return it.hasNext() || key != null;
	}

	@Override
	public DBVar[] next() {
		DBVar[] res = new DBVar[expressions.length];
		while (it.hasNext()) {
			DBVar[] row = it.next();
			DBVar[] thisKey = getKey(row);
			if (Arrays.deepEquals(key, thisKey)) { // still aggregating the same key
				aggregateRow(row);
				// key doesnt change so there is no need to update it
			} else { // finished with key
				for (int i = 0; i < aggs.length; i++) {
					res[i] = aggs[i].getVal();
					aggs[i].reset();
				}
				aggregateRow(row);
				key = thisKey; // setting key for next time
				if (having == null || having.testRow(res)) {
					return res;
				}
			}
		}
		// if we got here it means that the table ended
		// lets return all we have aggregated
		key = null;
		for (int i = 0; i < aggs.length; i++) {
			Aggregator agg = aggs[i];
			res[i] = agg.getVal();
		}
		return res;
	}

	private void aggregateRow(DBVar[] row) {
		for (int i = 0; i < aggs.length; i++) {
			Aggregator agg = aggs[i];
			agg.aggregate(row[selectedCols[i]]);
		}
	}
}
