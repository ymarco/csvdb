package commands;

import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.Statement;
import commands.select.Where;
import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;
import schema.Column;
import schema.DBVar;
import schema.Schema;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;


public class Select implements Command {
	private String outputName; // this can a file name in case of exporting to csv or a table name in other cases
	private Schema srcSchema;
	private Expression[] expressions;
	private Statement where;
	private Statement groupBy;
	private Statement orderBy;
	private Mode mode;

	private static Statement EmptyStatementOrNull(Statement s) {
		return (s == null ? Statement.emptyStatement : s);
	}

	public Select(String outputName, String srcTableName, Expression[] expressions,
	              Where where, GroupBy groupBy, OrderBy orderBy) throws Schema.NotFoundException {
		this(outputName, srcTableName, expressions, where, groupBy, orderBy, Mode.PRINT_TO_SCREEN);
	}

	public Select(String outputName, String srcTableName, Expression[] expressions,
	              Where where, GroupBy groupBy, OrderBy orderBy, Mode mode) throws Schema.NotFoundException {
		this.outputName = outputName;
		this.srcSchema = Schema.GetSchema(srcTableName);
		this.expressions = expressions;
		this.where = EmptyStatementOrNull(where);
		this.orderBy = EmptyStatementOrNull(orderBy);
		this.groupBy = groupBy;
		this.mode = mode;
	}

	public static enum Mode {PRINT_TO_SCREEN, EXPORT_TO_CSV, CREATE_NEW_TABLE}

	private static String[] rowToString(DBVar[] row) {
		String[] res = new String[row.length];
		for (int i = 0; i < row.length; i++) {
			res[i] = row[i].toString();
		}
		return res;
	}

	Stream<DBVar[]> getNewTableStream() {
		Stream<DBVar[]> s = srcSchema.getTableStream();
		// selectedColumns[i] is the index of the source column of column i in the new table
		int[] selectedColumns = Arrays.stream(expressions).map(e -> e.fieldName).mapToInt(srcSchema::getColumnIndex).toArray();
		s = where.apply(s);
		s = orderBy.apply(s);
		if (groupBy == null) { // no group by TODO: there CAN be aggregator functions here, this assumes there cant
			s =  s.map(reformatColumns(selectedColumns));
		} else {
			s = groupBy.apply(s); //TODO
		}
		return s;
	}

	public void run() {
		Stream<DBVar[]> finalStream = getNewTableStream();
		switch (mode) {
			case PRINT_TO_SCREEN:
				printToScreen(finalStream);
				break;
			case EXPORT_TO_CSV:
				System.out.println("select: exporting to file " + outputName);
				exportToCSV(String.join(File.separator, outputName), finalStream);
				break;
			case CREATE_NEW_TABLE:
				createNewTable(Schema.GetSchema(outputName), finalStream);
				break;
		}
	}

	private void createNewTable(Schema schema, Stream<DBVar[]> s) {
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
			//appender = writer.append(new File(outfileName), StandardCharsets.UTF_8);
			appender = writer.append(new File(outfileName), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// export to the csv
		s.map(Select::rowToString)
				.forEach(a -> {
					try {
						System.out.println("expoting " + Arrays.toString(a));
						appender.appendLine(a);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		try {
			appender.endLine();
			appender.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void printToScreen(Stream<DBVar[]> s) {
		s = s.limit(200); //no need to clutter the screen
		s.forEach(System.out::println);
	}

	private Schema createNewSchema() {
		if (expressions == null) {
			expressions = new Expression[srcSchema.getLinesCount()];
			for (int i = 0; i < expressions.length; i++)
				expressions[i] = new Expression(srcSchema.getColumnName(i));
		}

		Column[] columns = new Column[expressions.length];
		for (int i = 0; i < columns.length; i++) {
			Column column = srcSchema.getColumn(expressions[i].fieldName);
			columns[i] = new Column(column.type, expressions[i].asName);
		}
		new Create(outputName, false, columns).run();
		return Schema.GetSchema(outputName);
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