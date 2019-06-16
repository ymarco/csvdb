package schema;

import java.io.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.stream.Stream;

import commandLine.Main;

public class Schema {
	private static Hashtable<String, Schema> schemas = new Hashtable<String, Schema>();

	private String tableName;
	private String tablePath;
	private String tableFilePath;
	private Column[] columns;
	private Hashtable<String, Integer> fieldNameToIndex = new Hashtable<String, Integer>();
	private int lineCount = -1;
	private DBVar[][] table = null;

	public Schema(String tableName, Column[] columns) {
		this.tableName = tableName;
		this.columns = columns;
		this.tablePath = String.join(File.separator, Main.rootdir, tableName);
		tableFilePath = String.join(File.separator, tablePath, "data.ser");

		for (int i = 0; i < columns.length; i++)
			fieldNameToIndex.put(columns[i].name, i);
	}


	public Column getColumn(int i) {
		return columns[i];
	}


	public int getColumnIndex(String columnName) {
		return fieldNameToIndex.get(columnName);
	}

	public Column getColumn(String columnName) {
		return getColumn(getColumnIndex(columnName));
	}

	public String getColumnName(int i) {
		return getColumn(i).name;
	}

	public DBVar.Type getColumnType(int i) {
		return getColumn(i).type;
	}

	public DBVar.Type getColumnType(String columnName) {
		return getColumnType(getColumnIndex(columnName));
	}

	public int getColumnsCount() {
		return columns.length;
	}

	public String getTableName() {
		return tableName;
	}

	public String getTablePath() {
		return tablePath;
	}

	public void setLineCount(int lineCount) {
		this.lineCount = lineCount;
	}

	public int getLinesCount() {
		return lineCount;
	}


	public static void AddSchema(Schema schema) {
		schemas.put(schema.tableName, schema);
	}

	public static boolean HaveSchema(String tableName) {
		return schemas.containsKey(tableName);
	}

	public static Schema GetSchema(String tableName) throws NotFoundException {
		Schema schema = schemas.get(tableName);
		if (schema == null)
			throw new NotFoundException("schema \"" + tableName + "\" not found");
		return schema;
	}

	public static Schema RemoveSchema(String tableName) {
		return schemas.remove(tableName);
	}

	public String getColumnFileName(int i) {
		return getColumnName(i) + Main.columnFilesExtensios;
	}

	public String getColumnFileName(String columnName) {
		return getColumnFileName(getColumnIndex(columnName));
	}

	@Deprecated
	public String getColumnPath(int i) {
		return getTablePath() + "\\" + getColumnFileName(i);
	}

	@Deprecated
	public String getColumnPath(String columnName) {
		return getColumnPath(getColumnIndex(columnName));
	}

	private void loadTableToMem() {
		if (table != null) {
			return;
		}
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(tableFilePath));
			table = (DBVar[][]) in.readObject();
		} catch (IOException e) {
			throw new RuntimeException("table file for table " + tableName + " not found in path " + tableFilePath);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("error loading table in path " + tableFilePath);
		}
	}

	private void unloadTableFromMem() {
		table = null;
		/*
		 table is private and nothing else should have a reference to it
		 (except for streams using it)
		 so now it can hopefully be garbage collected.
		*/
	}

	private void loadTableToMemIfNotLoaded() {
		if (table == null)
			loadTableToMem();
	}

	public Stream<DBVar[]> getTableStream() {
		loadTableToMemIfNotLoaded();
		Stream<DBVar[]> res = Arrays.stream(table);
		unloadTableFromMem();
		return res;
	}

	public String getTableFilePath() {
		return tableFilePath;
	}

	public static class NotFoundException extends RuntimeException {
		NotFoundException(String msg) {
			super(msg);
		}
	}
}
