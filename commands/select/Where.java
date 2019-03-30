package commands.select;

import schema.DBVar;
import schema.Schema;
import schema.VarType;

//classes
public class Where {
	public final String fieldName;
	private DBVar constant;

	public interface TestCondition {
		boolean check(DBVar var);
	}

	public final TestCondition test;

	public Where(Schema schema, String fieldName, String operator, String constant_) {
		this.fieldName= fieldName;
		VarType vt = schema.getColumnType(fieldName);
		this.constant = parseConstant(constant_, vt);
		/* now creating the test function*/
		switch (operator) {
			case "<":
				switch (vt) {
					case INT:
						test = (var -> var.i < constant.i);
						break;
					case FLOAT:
						test = (var -> var.f < constant.f);
						break;
					case TIMESTAMP:
						test = (var -> Long.compareUnsigned(var.ts, constant.ts) < 0);
						break;
					case VARCHAR:
						throw new RuntimeException("invalid WHERE:\nWHERE VARCHAR < ...");
					default:
						test = null;
				}
				break;
			case "<=":
				switch (vt) {
					case INT:
						test = (var -> var.i <= constant.i);
						break;
					case FLOAT:
						test = (var -> var.f <= constant.f);
						break;
					case TIMESTAMP:
						test = (var -> Long.compareUnsigned(var.ts, constant.ts) <= 0);
						break;
					case VARCHAR:
						throw new RuntimeException("invalid WHERE:\nWHERE VARCHAR <= ...");
					default:
						test = null;
				}
				break;
			case ">":
				switch (vt) {
					case INT:
						test = (var -> var.i > constant.i);
						break;
					case FLOAT:
						test = (var -> var.f > constant.f);
						break;
					case TIMESTAMP:
						test = (var -> Long.compareUnsigned(var.ts, constant.ts) > 0);
						break;
					case VARCHAR:
						throw new RuntimeException("invalid WHERE:\nWHERE VARCHAR > ...");
					default:
						test = null;
				}
				break;
			case ">=":
				switch (vt) {
					case INT:
						test = (var -> var.i >= constant.i);
						break;
					case FLOAT:
						test = (var -> var.f >= constant.f);
						break;
					case TIMESTAMP:
						test = (var -> Long.compareUnsigned(var.ts, constant.ts) >= 0);
						break;
					case VARCHAR:
						throw new RuntimeException("invalid WHERE:\nWHERE VARCHAR >= ...");
					default:
						test = null;
				}
				break;
			case "==":
				switch (vt) {
					case INT:
						test = (var -> var.i == constant.i);
						break;
					case FLOAT:
						test = (var -> var.f == constant.f);
						break;
					case TIMESTAMP:
						test = (var -> var.ts == constant.ts);
						break;
					case VARCHAR:
						test = (var -> var.s.equals(constant.s));
						break;
					default:
						test = null;
				}
				break;
			case "<>":
				switch (vt) {
					case INT:
						test = (var -> var.i != constant.i);
						break;
					case FLOAT:
						test = (var -> var.f != constant.f);
						break;
					case TIMESTAMP:
						test = (var -> var.ts != constant.ts);
						break;
					case VARCHAR:
						test = (var -> !var.s.equals(constant.s));
						break;
					default:
						test = null;
				}
				break;
			case "is":
				switch (vt) {
					case INT:
						if (constant.i != DBVar.NULL_INT)
							throw new RuntimeException("invalid WHERE: where FIELD is <something that isn't NULL>");
						test = (var -> var.i == DBVar.NULL_INT);
						break;
					case FLOAT:
						if (constant.f != DBVar.NULL_FLOAT)
							throw new RuntimeException("invalid WHERE: where FIELD is <something that isn't NULL>");
						test = (var -> var.f == DBVar.NULL_FLOAT);
						break;
					case TIMESTAMP:
						if (constant.ts != DBVar.NULL_TS)
							throw new RuntimeException("invalid WHERE: where FIELD is <something that isn't NULL>");
						test = (var -> var.ts == DBVar.NULL_TS);
						break;
					case VARCHAR:
						if (constant.s.equals(DBVar.NULL_STRING))
							throw new RuntimeException("invalid WHERE: where FIELD is <something that isn't NULL>");
						test = (var -> var.s.equals(DBVar.NULL_STRING));
						break;
					default:
						test = null;
				}
				break;
			case "is not":
				switch (vt) {
					case INT:
						if (constant.i != DBVar.NULL_INT)
							throw new RuntimeException("invalid WHERE: where FIELD is <something that isn't NULL>");
						test = (var -> var.i != DBVar.NULL_INT);
						break;
					case FLOAT:
						if (constant.f != DBVar.NULL_FLOAT)
							throw new RuntimeException("invalid WHERE: where FIELD is <something that isn't NULL>");
						test = (var -> var.f != DBVar.NULL_FLOAT);
						break;
					case TIMESTAMP:
						if (constant.ts != DBVar.NULL_TS)
							throw new RuntimeException("invalid WHERE: where FIELD is <something that isn't NULL>");
						test = (var -> var.ts != DBVar.NULL_TS);
						break;
					case VARCHAR:
						if (constant.s.equals(DBVar.NULL_STRING))
							throw new RuntimeException("invalid WHERE: where FIELD is <something that isn't NULL>");
						test = (var -> !var.s.equals(DBVar.NULL_STRING));
						break;
					default:
						test = null;
				}
				break;
			default:
				throw new RuntimeException("invalid where operator");

		}
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
