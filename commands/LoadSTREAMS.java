package commands;

import de.siegmar.fastcsv.reader.RowReader;
import schema.DBVar;
import schema.dbvars.DBFloat;
import schema.Schema;
import schema.dbvars.DBInt;
import schema.dbvars.DBTS;
import schema.dbvars.DBVarchar;
import utils.FilesUtils;

import java.io.*;

public class LoadSTREAMS implements Command {
	private String fileName;
	private String tableName;
	private int ignoreLines;

	public LoadSTREAMS(String fileName, String tableName, int ignoreLines) {
		this.fileName = fileName;
		this.tableName = tableName;
		this.ignoreLines = ignoreLines;
	}

	public void run() {
		try {
			createFiles(fileName, tableName, ignoreLines);
		} catch (IOException e) {
			throw new RuntimeException("idk");
		}
	}

	private void createFiles(String fileName, String tableName, int ignoreLines) throws IOException {
		if (!Schema.HaveSchema(tableName))
			throw new RuntimeException("you tried to load a non existing table");
		Schema schema = Schema.GetSchema(tableName);
		int linesCount = FilesUtils.countLines(tableName);
		DBVar[][] table = new DBVar[linesCount][schema.getColumnsCount()];
		RowReader file = new RowReader(new FileReader(fileName), ',', '"');
		// read past the lines that should be ignored
		while (ignoreLines > 0 && !file.isFinished()) {
			file.readLine();
			ignoreLines--;
		}
		// load from csv to table
		int lineNumber = 0;
		while (!file.isFinished()) {
			//read
			RowReader.Line line = file.readLine();
			String[] row = line.getFields();
			//write
			for (int colNumber = 0; colNumber < row.length; colNumber++) {
				try {
					String curr = row[colNumber];
					switch (schema.getColumnType(colNumber)) {
						case INT:
							table[lineNumber][colNumber] = new DBInt(curr);
							break;
						case TS:
							table[lineNumber][colNumber] = new DBTS(curr);
							break;
						case FLOAT:
							table[lineNumber][colNumber] = new DBFloat(curr);
							break;
						case VARCHAR:
							table[lineNumber][colNumber] = new DBVarchar(curr);
							break;
					}
				} catch (NumberFormatException e) { // TODO narrow the exception
					file.close();
					throw new RuntimeException("you tried to load file to invalid csv;" +
							" couldnt format it into a valid table.");
				}

			}
			lineNumber++;
		}
		schema.setLineCount(lineNumber);

	writeTable(table, schema.getTablePath());
	}

	public static void writeTable(DBVar[][] table, String path) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
		out.writeObject(table);
	}
}
