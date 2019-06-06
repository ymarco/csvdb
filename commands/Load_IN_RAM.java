package commands;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import cmd.Main;
import de.siegmar.fastcsv.reader.RowReader;
import schema.Schema;
import schema.VarType;
import utils.FilesUtils;

import static schema.VarType.FLOAT;
import static schema.VarType.VARCHAR;

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
			createFiles(fileName, tableName, ignoreLines);
		} catch (IOException e) {
			throw new RuntimeException("idk");
		}
	}

	private void createFiles(String fileName, String tableName, int ignoreLines) throws IOException {
		if (!Schema.HaveSchema(tableName))
			throw new RuntimeException("you tried to load a non existing table");
		RowReader file = new RowReader(new FileReader(fileName), ',', '"');
		// read past the lines that should be ignored
		while (ignoreLines > 0 && !file.isFinished()) {
			file.readLine();
			ignoreLines--;
		}
		//create BufferedReaders
		Schema schema = Schema.GetSchema(tableName);
		Object[] outLists = new Object[schema.getColumnsCount()];
		for (int i = 0; i < schema.getColumnsCount(); i++) {
			switch (schema.getColumnType(i)) {
				case INT:
					outLists[i] = new ArrayList<Integer>();
					break;
				case TIMESTAMP:
					outLists[i] = new ArrayList<Long>();
					break;
				case FLOAT:
					outLists[i] = new ArrayList<Double>();
					break;
				case VARCHAR:
					outLists[i] = new ArrayList<Long>();
					break;
			}
		}
		// load from csv to the lists
		int lineCount = 0;
		while (!file.isFinished()) {
			//read
			RowReader.Line line = file.readLine();
			String[] fields = line.getFields();
			//write
			for (int i = 0; i < fields.length; i++) {
				try {
					switch (schema.getColumnType(i)) {
						case INT:
							((List<Integer>) outLists[i]).add(Integer.parseInt(fields[i]));
							break;
						case TIMESTAMP:
							((List<Long>) outLists[i]).add(Long.parseLong(fields[i]));
							break;
						case FLOAT:
							((List<Double>) outLists[i]).add(Double.parseDouble(fields[i]));
							break;
						case VARCHAR:
							((List<String>) outLists[i]).add(fields[i]);
							break;
					}
				} catch (Exception e) { // TODO narrow the exception
					file.close();
					throw new RuntimeException("you tried to load file to invalid table");
				}

			}
			lineCount++;
		}
		schema.setLineCount(lineCount);

		//convert lists to pure arrays and serialize them
		for (int i = 0; i < schema.getColumnsCount(); i++) {
			switch (schema.getColumnType(i)) {
				case INT: {
					int[] toSave = new int[schema.getLinesCount()];
					List<Integer> savingSrc = (List<Integer>) outLists[i];
					for (int j = 0; j < savingSrc.size(); j++) {
						toSave[i] = savingSrc.get(i);
					}
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(schema.getColumn(i).path));
					out.writeObject(toSave);
					break;
				}

				case TIMESTAMP: {
					long[] toSave = new long[schema.getLinesCount()];
					List<Long> savingSrc = (List<Long>) outLists[i];
					for (int j = 0; j < savingSrc.size(); j++) {
						toSave[j] = savingSrc.get(j);
					}
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(schema.getColumn(i).path));
					out.writeObject(toSave);
					break;
				}

				case FLOAT: {
					double[] toSave = new double[schema.getLinesCount()];
					List<Double> savingSrc = (List<Double) outLists[i];
					for (int j = 0; j < savingSrc.size(); j++) {
						toSave[i] = savingSrc.get(i);
					}
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(schema.getColumn(i).path));
					out.writeObject(toSave);
					break;
				}

				case VARCHAR: {
					String[] toSave = new String[schema.getLinesCount()];
					List<String> savingSrc = (List<String>) outLists[i];
					for (int j = 0; j <; j++) {
						toSave[i] = savingSrc.get(i);
					}
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(schema.getColumn(i).path));
					out.writeObject(toSave);
					break;
				}
			}
		}
	}
}
