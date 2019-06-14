package commands;

import commands.select.*;
import schema.Column2;
import schema.DBVar;
import schema.Schema;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;


public class SelectSTREAMS implements Command {
	private String newTableName;
	private Schema srcSchema;
	private Expression[] expressions;
	private Statement where; //don't work now
	private Statement groupBy; //don't work now
	private Statement orderBy; //don't work now

	private static Statement EmptyStatementOrNull(Statement s) {
		return (s == null ? Statement.emptyStatement : s);
	}

	public SelectSTREAMS(String tableName, String srcTableName, Expression[] expressions,
	                     WhereSTREAMS where, GroupBySTREAMS groupBy, OrderBySTREAMS orderBy) throws Schema.NotFoundException {
		this.newTableName = tableName;
		this.srcSchema = Schema.GetSchema(srcTableName);
		this.expressions = expressions;
		this.where = EmptyStatementOrNull(where);
		this.orderBy = EmptyStatementOrNull(orderBy);
		this.groupBy = groupBy;
	}

	public void run() {
		Schema newSchema = createNewSchema();
		Stream<DBVar[]> s = srcSchema.getTableStream();
		s = where.apply(s);
		s = orderBy.apply(s);
		if (groupBy == null) { // no group by
			// selectedColumns[i] is the index of the source column of column i in the new table
			int[] selectedColumns = Arrays.stream(expressions).map(e -> e.fieldName).mapToInt(srcSchema::getColumnIndex).toArray();
			DBVar[][] newTabse = s.map(
					va -> {
						DBVar[] res = new DBVar[selectedColumns.length];
						for (int i = 0; i < selectedColumns.length; i++) {
							res[i] = va[selectedColumns[i]];
						}
						return res;
					}).toArray(DBVar[][]::new);

			try {
				LoadSTREAMS.writeTable(newTabse, newSchema.getTablePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			s = groupBy.apply(s);
		}
	}

	private Schema createNewSchema() {
		if (expressions == null) {
			expressions = new Expression[srcSchema.getLinesCount()];
			for (int i = 0; i < expressions.length; i++)
				expressions[i] = new Expression(srcSchema.getColumnName(i));
		}

		Column2[] columns = new Column2[expressions.length];
		for (int i = 0; i < columns.length; i++) {
			Column2 column = srcSchema.getColumn(expressions[i].fieldName);
			columns[i] = new Column2(column.type, expressions[i].asName, null /*for compile*/);
		}
		new Create(newTableName, false, columns).run();
		return Schema.GetSchema(newTableName);
	}


	// what is this? needs comments TODO
	public static class Expression {
		public enum AggFuncs {NOTHING, MIN, MAX, AVG, SUM, COUNT}

		;

		public String fieldName;
		public String asName;
		public AggFuncs aggFunc;

		public Expression(String fieldName) {
			this(fieldName, AggFuncs.NOTHING);
		}

		public Expression(String fieldName, AggFuncs aggFunc) {
			this(fieldName, fieldName, aggFunc);
		}

		public Expression(String fieldName, String asName) {
			this(fieldName, asName, AggFuncs.NOTHING);
		}

		public Expression(String fieldName, String asName, AggFuncs aggFunc) {
			this.fieldName = fieldName;
			this.asName = asName;
			this.aggFunc = aggFunc;
		}
	}


}