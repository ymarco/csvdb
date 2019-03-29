package schema;

import enums.VarType;

public class Column {
	public final VarType type;
	public final String name;
	
//	public float avg;
//	public long min;
//	public long max;
	
	public Column(VarType type, String name) {
		this.type = type;
		this.name = name;
	}
}
