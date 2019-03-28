package schema;

import enums.VarType;

public class Column {
	private VarType type;
	private String name;
	
//	public float avg;
//	public long min;
//	public long max;
	
	public Column(VarType type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public VarType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
}
