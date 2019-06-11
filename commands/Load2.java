package commands;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import commandLine.Main;
import de.siegmar.fastcsv.reader.RowReader;
import schema.DBVar;
import schema.Schema;
import utils.FilesUtils;

public class Load2 implements Command {
	private String fileName;
	private String tableName;
	private int ignoreLines;
	
	public Load2(String fileName, String tableName, int ignoreLines) {
		this.fileName = fileName;
		this.tableName = tableName;
		this.ignoreLines = ignoreLines;
	}
	
	public void run() {
		createFiles(fileName, tableName, ignoreLines);
	}
	
	private void createFiles(String fileName, String tableName, int ignoreLines) {
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
				if (schema.getColumnType(i) == DBVar.Type.VARCHAR)
					outFiles[i] = new BufferedWriter(new FileWriter(schema.getTablePath() + "\\" + schema.getColumnName(i) + Main.columnFilesExtensios));
				else
					outFilesBin[i] = new DataOutputStream(new FileOutputStream(schema.getTablePath() + "\\" + schema.getColumnName(i) + Main.columnFilesExtensios));
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
						case TS:
							outFilesBin[i].writeLong(Long.parseUnsignedLong(fields[i]));
							break;
						case FLOAT:
							outFilesBin[i].writeFloat(Float.parseFloat(fields[i]));
							break;
						case VARCHAR:
							//outFiles[i].write(FilesUtils.endoceStringForWriting(fields[i]) + "\n"); //string/0
							outFiles[i].write(fields[i] + "\0");
							break;
						}
					} catch (Exception e) {
						file.close();
						throw new RuntimeException("you tried to load file to invalid table");
					}
					
				}
				lineCount++;
			}
			schema.setLineCount(lineCount);
			
			//close
			file.close();
			FilesUtils.closeAll(outFiles);
			FilesUtils.closeAll(outFilesBin);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("^^Error in LOAD^^");
		}
	}
}