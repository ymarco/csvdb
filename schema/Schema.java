package schema;

import java.util.Hashtable;


public class Schema {
	Column[] columns;
	Hashtable<String, Integer> fieldNameToIndex = new Hashtable<String, Integer>();
	int lineCount = -1;
	
	public Schema(Column[] columns) {
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
}
