package commands.select;

import schema.DBVar;
import schema.Schema;

public class Row {
	private DBVar[] items;
	private Schema schema;
	
	public Row(DBVar[] items, Schema schema) {
		this.items = items;
		this.schema = schema;
	}
	
	public DBVar get(String fieldName) {
		return get(schema.getColumnIndex(fieldName));
	}
	
	public DBVar get(int i) {
		return items[i];
	}
	
	public void set(String fieldName, DBVar var) {
		set(schema.getColumnIndex(fieldName), var);
	}
	
	public void set(int i, DBVar var) {
		items[i] = var;
	}
}