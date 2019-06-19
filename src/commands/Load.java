package commands;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import schema.DBVar;
import schema.Schema;
import schema.dbvars.DBFloat;
import schema.dbvars.DBInt;
import schema.dbvars.DBTS;
import schema.dbvars.DBVarchar;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class Load implements Command {
	private String fileName;
	private String tableName;
	private int ignoreLines;

	public Load(String fileName, String tableName, int ignoreLines) {
		this.fileName = fileName;
		this.tableName = tableName;
		this.ignoreLines = ignoreLines;
	}

	public void run() {
		try {
			loadToSerializedArray(fileName, tableName, ignoreLines);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadToSerializedArray(String fileName, String tableName, int ignoreLines) throws IOException {
		Schema schema = Schema.GetSchema(tableName);
		List<DBVar[]> tableFromCSV = new ArrayList<>();
		CsvReader csvReader = new CsvReader();
		CsvParser parser = csvReader.parse(new File(fileName), StandardCharsets.UTF_8);
		CsvRow row;
		// read past the lines that should be ignored
		for (int i = 0; i < ignoreLines; i++) {
			try {
				row = parser.nextRow();
				if (row == null) throw new IOException();
			} catch (IOException e) {
				break;
			}
		}
		// load from csv to table
		int lineNumber = 0;
		while (true) {
			//read
			try {
				row = parser.nextRow();
				if (row == null) break;
			} catch (IOException e) {
				break;
			}
			DBVar[] parsedRow = new DBVar[schema.getColumnsCount()];
			//tableList.add(new DBVar[schema.getColumnsCount()]);
			//write
			for (int colNumber = 0; colNumber < row.getFieldCount(); colNumber++) {
				String curr = row.getField(colNumber);
				//System.out.println("row[" + colNumber + "] = " + curr);
				try {
					parsedRow[colNumber] = parsesVar(schema.getColumnType(colNumber), curr);
				} catch (NumberFormatException e) {
					parser.close();
					throw new RuntimeException("you tried to load an invalid csv;" +
							" couldnt format it into a valid table." +
							"[error parsing '" + curr + "' to a " + schema.getColumnType(colNumber));
				}

			}
			lineNumber++;
			tableFromCSV.add(parsedRow);
		}
		int CSVLinesCount = lineNumber + 1;

		schema.setLineCount(schema.getLinesCount() + CSVLinesCount);

		// concat current table with the one read from the csv
		Stream<DBVar[]> joined = Stream.concat(schema.getTableStream(), tableFromCSV.stream());
		DBVar[][] newTable = joined.toArray(DBVar[][]::new);
		File oldSerialization = new File(schema.getTableFilePath());
		if (oldSerialization.exists()) {
			System.out.println("old table " + schema.getTableName() + " exists");
			oldSerialization.delete();
		}
		writeTable(newTable, schema.getTableFilePath());
		parser.close();
	}

	private DBVar parsesVar(DBVar.Type type, String curr) throws NumberFormatException {
		switch (type) {
			case INT:
				return new DBInt(curr);
			case TS:
				return new DBTS(curr);
			case FLOAT:
				return new DBFloat(curr);
			case VARCHAR:
				return new DBVarchar(curr);
		}
		return null;
	}

	static void writeTable(DBVar[][] table, String path) throws IOException {
		try(FSTObjectOutput out =
				    new FSTObjectOutput(
				    		new GZIPOutputStream(
				    				new FileOutputStream(path)))) {
			out.writeObject(table);
		}
	}
}
