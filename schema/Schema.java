package schema;

import java.util.Hashtable;

public class Schema {
	private static Hashtable<String, Schema> schemas = new Hashtable<String, Schema>();
	
	String tableName;
	Column[] columns;
	Hashtable<String, Integer> fieldNameToIndex = new Hashtable<String, Integer>();
	int lineCount = -1;
	
	private Schema(String tableName, Column[] columns) {
		this.tableName = tableName;
		this.columns = columns;
		
		for (int i = 0; i < columns.length; i++)
			fieldNameToIndex.put(columns[i].getName(), i);
	}
	
	
	public Column getColumn(int i) {
		return columns[i];
	}
	
	public Column getColumn(String columnName) {
		return columns[fieldNameToIndex.get(columnName)];
	}
	
	public int getColumnIndex(String columnName) {
		return fieldNameToIndex.get(columnName);
	}
	
	public String getColumnName(int i) {
		return columns[i].getName();
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
	
	
}
