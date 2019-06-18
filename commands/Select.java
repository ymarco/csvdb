package commands;

import commands.select.*;
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
	private SelectExpression[] expressions;
	private Statement where;
	private Statement groupBy;
	private Statement orderBy;
	private Mode mode;

	private static Statement EmptyStatementOrNull(Statement s) {
		return (s == null ? Statement.emptyStatement : s);
	}

	public Select(String outputName, String srcTableName, SelectExpression[] expressions,
	              Where where, GroupBy groupBy, OrderBy orderBy) throws Schema.NotFoundException {
		this(outputName, srcTableName, expressions, where, groupBy, orderBy, Mode.PRINT_TO_SCREEN);
	}

	public Select(String outputName, String srcTableName, SelectExpression[] expressions,
	              Where where, GroupBy groupBy, OrderBy orderBy, Mode mode) throws Schema.NotFoundException {
		this.outputName = outputName;
		this.srcSchema = Schema.GetSchema(srcTableName);
		this.expressions = expressions;
		this.where = EmptyStatementOrNull(where);
		this.orderBy = EmptyStatementOrNull(orderBy);
		this.groupBy = groupBy;
		this.mode = mode;
	}

	public enum Mode {PRINT_TO_SCREEN, EXPORT_TO_CSV, CREATE_NEW_TABLE}

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
		if (groupBy == null) {
			s = orderBy.apply(s);
			s = s.map(reformatColumns(selectedColumns));
		} else {
			s = groupBy.apply(s);
			s = orderBy.apply(s);
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
				exportToCSV(String.join(File.separator, outputName), finalStream);
				break;
			case CREATE_NEW_TABLE:
				createNewTable(outputName, finalStream);
				break;
		}
	}

	private void createNewTable(String newTableName, Stream<DBVar[]> s) {
		DBVar[][] newTable = s.toArray(DBVar[][]::new);
		// createAsSelect should have created a schema for us
		Schema schema = Schema.GetSchema(newTableName);
		try {
			Load.writeTable(newTable, schema.getTableFilePath());
			schema.setLineCount(newTable.length);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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


}