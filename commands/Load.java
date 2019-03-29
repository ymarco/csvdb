package commands;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.siegmar.fastcsv.reader.RowReader;
import enums.VarType;
import schema.Schema;

public class Load {
	public static void run(String fileName, String tableName, int ignoreLines) {
		createFiles(fileName, tableName, ignoreLines);
	}
	
	private static void createFiles(String fileName, String tableName, int ignoreLines) {
		if (!Schema.HaveSchema(tableName))
			throw new RuntimeException("you tried to load a non existing table");
		try {
			RowReader file = new RowReader(new FileReader(fileName), ',', '"');
			// ignore lines
			while (ignoreLines > 0 && !file.isFinished()) {
				file.readLine();
				ignoreLines--;
			}
			//create BufferedReaders
			Schema schema = Schema.GetSchema(tableName);
			BufferedWriter[] outFiles = new BufferedWriter[schema.getColumnsCount()];
			DataOutputStream[] outFilesBin = new DataOutputStream[schema.getColumnsCount()];
			for (int i = 0; i < outFiles.length; i++) {
				if (schema.getColumnType(i) == VarType.VARCHAR)
					outFiles[i] = new BufferedWriter(new FileWriter(schema.getTablePath() + "\\" + schema.getColumnName(i) + ".onym"));
				else
					outFilesBin[i] = new DataOutputStream(new FileOutputStream(schema.getTablePath() + "\\" + schema.getColumnName(i) + ".onym"));
			}
			
			// put in other files
			int lineCount = 0;
			while (!file.isFinished()) {
				//read
				RowReader.Line l = file.readLine();
				String[] fields = l.getFields();
				//write
				for (int i = 0; i < fields.length; i++) {
					try {
						switch (schema.getColumnType(i)) {
						case INT:
							outFilesBin[i].writeLong(Long.parseLong(fields[i]));
							break;
						case TIMESTAMP:
							outFilesBin[i].writeLong(Long.parseUnsignedLong(fields[i]));
							break;
						case FLOAT:
							outFilesBin[i].writeFloat(Float.parseFloat(fields[i]));
							break;
						case VARCHAR:
							String item = fields[i];
							item = item.replace("\\", "\\\\");
							item = item.replace("\r\n", "\\n");
							outFiles[i].write(item + "\n");
							break;
						}
					} catch (Exception e) {
						throw new RuntimeException("you tried to load file to invalid table");
					}
					
				}
				lineCount++;
			}
			schema.setLineCount(lineCount);
			//close
			file.close();
			for (int i = 0; i < outFiles.length; i++) {
				if (schema.getColumnType(i) == VarType.VARCHAR)
					outFiles[i].close();
				else
					outFilesBin[i].close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("^^Error in LOAD^^");
		}
	}
}