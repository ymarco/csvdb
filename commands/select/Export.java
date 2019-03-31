package commands.select;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import schema.Schema;
import schema.VarType;
import utils.FilesUtils;

public class Export {

	public static void export(String  toFile) {
		export("#export", toFile);
	}

	public static void export(String tableName, String toFile) {
		try {
			Schema schema = Schema.GetSchema(tableName);
			DataInputStream[] inputFilesBin = new DataInputStream[schema.getColumnsCount()];
			BufferedReader[] inputFiles = new BufferedReader[schema.getColumnsCount()];
			//open
			for (int i = 0; i < schema.getColumnsCount(); i++)
				if (schema.getColumnType(i) == VarType.VARCHAR)
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