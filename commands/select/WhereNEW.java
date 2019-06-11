package commands.select;

import schema.DBVar;
import schema.Schema;

import java.util.Comparator;

//classes
public class WhereNEW {
	public final String fieldName;
	private DBVar constant;

	public interface TestCondition {
		boolean check(DBVar var);
	}

	public final TestCondition test;

	public WhereNEW(Schema schema, String fieldName, String operator, String constant_) {
		this.fieldName = fieldName;
		DBVar.Type vt = schema.getColumnType(fieldName);
		//this.constant = parseConstant(constant_, vt);

		try {
			switch (vt) {
				case INT:
					this.constant = new schema.dbvars.Int(constant_);
					break;
				case FLOAT:
					this.constant = new schema.dbvars.Float(constant_);
					break;
				case VARCHAR:
					this.constant = new schema.dbvars.Varchar(constant_);
					break;
				case TS:
					this.constant = new schema.dbvars.TS(constant_);
					break;
			}
		} catch (NumberFormatException e) {
            throw new RuntimeException("invalid constant for where: " + constant_);
		}
		/* now creating the test function*/
		Comparator<DBVar> comparator = this.constant.comparator();
		DBVar const_null = constant.getNull();
		switch (operator) {
			case "<":
				test = v -> comparator.compare(constant, v) < 0;
				break;
			case "<=":
				test = v -> comparator.compare(constant, v) <= 0;
				break;
			case ">":
				test = v -> comparator.compare(constant, v) > 0;
				break;
			case ">=":
				test = v -> comparator.compare(constant, v) >= 0;
				break;
			case "<>":
				test = v -> comparator.compare(constant, v) != 0;
				break;
			case "is":
				test = v -> comparator.compare(const_null, v) != 0;
				break;
			case "is not":
				test = v -> comparator.compare(const_null.getNull(), v) == 0;
				break;
			default:
				throw new RuntimeException("invalid where operator");

		}
	}
}
