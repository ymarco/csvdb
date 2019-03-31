package schema;

import java.util.Hashtable;

import cmd.Main;

public class Schema {
	private static Hashtable<String, Schema> schemas = new Hashtable<String, Schema>();
	
	private String tableName;
	private String tablePath;
	private Column[] columns;
	private Hashtable<String, Integer> fieldNameToIndex = new Hashtable<String, Integer>();
	private int lineCount = -1;
	
	private Schema(String tableName, Column[] columns) {
		this.tableName = tableName;
		this.columns = columns;
		this.tablePath = Main.rootdir + "\\" + tableName;
		
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
	
	public VarType getColumnType(int i) {
		return getColumn(i).type;
	}
	
	public VarType getColumnType(String columnName) {
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
	
	
	
	public static void AddSchema(String tableName, Column[] columns) {
		schemas.put(tableName, new Schema(tableName, columns));
	}
	
	public static boolean HaveSchema(String tableName) {
		return schemas.containsKey(tableName);
	}

	public static Schema GetSchema(String tableName) {
		return schemas.get(tableName);
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
	
	public String getColumnPath(int i) {
		return getTablePath() + "\\" + getColumnFileName(i);
	}

	public String getColumnPath(String columnName) {
		return getColumnPath(getColumnIndex(columnName));
	}
}
