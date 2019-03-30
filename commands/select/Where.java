package commands.select;

import commands.Select;
import schema.DBVar;
import schema.Schema;
import schema.VarType;

//classes
public class Where {
	private final Schema schema;
	public final String fieldName;
	private DBVar constant;

	public interface TestCondition {
		boolean check(DBVar var);
	}

	public final TestCondition test;

	public Where(Schema schema, String fieldName, String operator, String constant_) {
		this.schema = schema;
		this.fieldName= fieldName;
		this.constant = parseConstant(constant_);
		VarType vt = schema.getColumnType(fieldName);
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
						throw new RuntimeException("invalid WHERE:" +
								"WHERE VARCHAR < ...");
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
						throw new RuntimeException("invalid WHERE:" +
								"WHERE VARCHAR <= ...");
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
						throw new RuntimeException("invalid WHERE:" +
								"WHERE VARCHAR > ...");
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
						throw new RuntimeException("invalid WHERE:" +
								"WHERE VARCHAR >= ...");
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
						throw new RuntimeException("invalid WHERE:" +
								"WHERE VARCHAR <> ...");
					default:
						test = null;
				}
				break;
			default:
				throw new RuntimeException("invalid where operator");

		}
	}

	private static DBVar parseConstant(String constant) {
		DBVar res = new DBVar();
		if (constant.equals("none")) {
			res.i = DBVar.NULL_INT;
			res.s = DBVar.NULL_STRING;
			res.f = DBVar.NULL_FLOAT;
			res.ts = DBVar.NULL_TS;
		} else {
			res.i = Long.parseLong(constant);
			res.s = constant;
			res.f = Double.parseDouble(constant);
			res.ts = Long.parseUnsignedLong(constant);
		}
		return res;
	}
}
