package commands;

import commands.select.*;
import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;
import schema.Column2;
import schema.DBVar;
import schema.Schema;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;


public class Select implements Command {
	private String newTableName;
	private Schema srcSchema;
	private Expression[] expressions;
	private Statement where;
	private Statement groupBy;
	private Statement orderBy;

	private static Statement EmptyStatementOrNull(Statement s) {
		return (s == null ? Statement.emptyStatement : s);
	}

	public Select(String tableName, String srcTableName, Expression[] expressions,
	              Where where, GroupBy groupBy, OrderBy orderBy) throws Schema.NotFoundException {
		this.newTableName = tableName;
		this.srcSchema = Schema.GetSchema(srcTableName);
		this.expressions = expressions;
		this.where = EmptyStatementOrNull(where);
		this.orderBy = EmptyStatementOrNull(orderBy);
		this.groupBy = groupBy;
	}

	private static enum Mode {PRINT_TO_SCREEN, EXPORT_TO_CSV, CREATE_NEW_TABLE}

	private static String[] rowToString(DBVar[] row) {
		String[] res = new String[row.length];
		for (int i = 0; i < row.length; i++) {
			res[i] = row[i].toString();
		}
		return res;
	}

	public void run(Mode mode) {
		Schema newSchema = createNewSchema();
		Stream<DBVar[]> s = srcSchema.getTableStream();
		s = where.apply(s);
		s = orderBy.apply(s);
		if (groupBy == null) { // no group by TODO: there CAN be aggregator functions here
			// selectedColumns[i] is the index of the source column of column i in the new table
			int[] selectedColumns = Arrays.stream(expressions).map(e -> e.fieldName).mapToInt(srcSchema::getColumnIndex).toArray();
			Stream<DBVar[]> finalStream = s.map(reformatColumns(selectedColumns));
			switch (mode) {
				case PRINT_TO_SCREEN:
					printToScreen(finalStream);
					break;
				case EXPORT_TO_CSV:
					String outfileName = newTableName + ".csv"; // TODO: get the actual name
					exportToCSV(outfileName, finalStream);
					break;
				case CREATE_NEW_TABLE:
					createNewTable(finalStream);
					break;
			}


		} else {
			s = groupBy.apply(s); //TODO
		}
	}

	private void createNewTable(Stream<DBVar[]> s) {
		DBVar[][] newTable = s.toArray(DBVar[][]::new);
		//TODO: load table into a new schema
	}

	private Function<DBVar[], DBVar[]> reformatColumns(int[] selectedColumns) {
		return row -> {
			DBVar[] newRow = new DBVar[selectedColumns.length];
			for (int i = 0; i < selectedColumns.length; i++) {
				newRow[i] = row[selectedColumns[i]];
			}
			return newRow;
		};
	}

	private void exportToCSV(String outfileName, Stream<DBVar[]> s) {
		// prepare csv stuff
		CsvWriter writer = new CsvWriter();
		CsvAppender appender;
		try {
			appender = writer.append(new File(outfileName), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// export to the csv
		s.map(Select::rowToString)
				.forEach(a -> {
					try {
						appender.appendLine(a);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
	}

	public void run() {
		run(Mode.PRINT_TO_SCREEN);
	}

	public static void printToScreen(Stream<DBVar[]> s) {
		s.limit(200); //no need to clutter the screen
		s.forEach(System.out::println);
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