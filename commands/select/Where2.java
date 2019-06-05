package commands.select;

import schema.DBVar;
import schema.Schema;
import schema.VarType;

//classes
public class Where2 {
	public final String fieldName;
	public final String operator;
	public DBVar constant;

	public Where2(Schema schema, String fieldName, String operator, String constant) {
		this.fieldName= fieldName;
		this.operator = operator;
		this.constant = parseConstant(constant, schema.getColumnType(fieldName));
	}

	private static DBVar parseConstant(String constant, VarType varType) {
		DBVar res = new DBVar();
		try {
			switch (varType) {
			case INT:
				res.i = constant.equals("null") ? DBVar.NULL_INT : Long.parseLong(constant); 
				return res;
			case FLOAT:
				res.f = constant.equals("null") ? DBVar.NULL_FLOAT : Double.parseDouble(constant); 
				return res;
			case TIMESTAMP:
				res.ts = constant.equals("null") ? DBVar.NULL_TS : Long.parseUnsignedLong(constant); 
				return res;
			case VARCHAR:
				res.s = constant.equals("null") ? DBVar.NULL_STRING : constant; 
				return res;
			}
		} catch (Exception e) {
			throw new RuntimeException("invalid WHERE: you tried to compare between two different types");
		}
		return null;
	}
}
