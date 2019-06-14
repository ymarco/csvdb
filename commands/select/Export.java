package commands.select;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import schema.DBVar;
import schema.Schema;
import utils.FilesUtils;

public class Export {

	public static void exportToCsv(String  toFile) {
		exportToCsv("#export", toFile);
	}

	public static void exportToCsv(String tableName, String toFile) {
		try {
			Schema schema = Schema.GetSchema(tableName);
			DataInputStream[] inputFilesBin = new DataInputStream[schema.getColumnsCount()];
			BufferedReader[] inputFiles = new BufferedReader[schema.getColumnsCount()];
			//open
			for (int i = 0; i < schema.getColumnsCount(); i++)
				if (schema.getColumnType(i) == DBVar.Type.VARCHAR)
					inputFiles[i] = new BufferedReader(new FileReader(schema.getColumnFileName(i)));
				else
					inputFilesBin[i] = new DataInputStream(new FileInputStream(schema.getColumnFileName(i)));
			BufferedWriter outFile = new BufferedWriter(new FileWriter(toFile));
			//fill outFile
			for (int i = 0; i < inputFiles.length; i++) {
				//TODO read and fill outputFile
			}
			FilesUtils.closeAll(inputFilesBin);
			FilesUtils.closeAll(inputFiles);
			outFile.close();
		} catch (IOException e) { }
	}
}