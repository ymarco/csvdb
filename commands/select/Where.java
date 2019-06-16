package commands.select;

import schema.DBVar;
import schema.Schema;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import schema.dbvars.DBInt;

//classes
public class Where implements Statement {
	public final int colNum;
	private DBVar constant;
	private Schema schema;

	public Predicate<DBVar> pred;

	public Where(Schema schema, int colNum, String operator, String constant_) {
		this.colNum = colNum;
		this.schema = schema;
		parseConstant(constant_);
		createPred(operator);
	}

	private void createPred(String operator) {
		if (constant == null) {
			pred = v -> false;
			return;
		}
		Comparator<DBVar> comparator = this.constant.comparator();
		switch (operator) {
			case "=":
				pred = v -> !v.isNull() && comparator.compare(v, constant) == 0;
				break;
			case "<":
				pred = v -> !v.isNull() && comparator.compare(v, constant) < 0;
				break;
			case "<=":
				pred = v -> !v.isNull() && comparator.compare(v, constant) <= 0;
				break;
			case ">":
				pred = v -> !v.isNull() && comparator.compare(v, constant) > 0;
				break;
			case ">=":
				pred = v -> !v.isNull() && comparator.compare(v, constant) >= 0;
				break;
			case "<>":
				pred = v -> !v.isNull() && comparator.compare(v, constant) != 0;
				break;
			case "is null":
				pred = v -> v.isNull();
				break;
			case "is not null":
				pred = v -> !v.isNull();
				break;
			default:
				throw new RuntimeException("invalid where operator '" + operator + "'");

		}
	}

	private void parseConstant(String constant_) {
		DBVar.Type vt = schema.getColumnType(colNum);
		if (constant_ == null) {
			this.constant = new DBInt(0);
			return;
		} else if (constant_.equals("null") && vt != DBVar.Type.VARCHAR) {
			this.constant = null;
			return;
		}
		try {
			switch (vt) {
				case INT:
					this.constant = new schema.dbvars.DBInt(constant_);
					break;
				case FLOAT:
					this.constant = new schema.dbvars.DBFloat(constant_);
					break;
				case VARCHAR:
					this.constant = new schema.dbvars.DBVarchar(constant_);
					break;
				case TS:
					this.constant = new schema.dbvars.DBTS(constant_);
					break;
			}
		} catch (NumberFormatException e) {
			throw new RuntimeException("invalid constant for where: " + constant_);
		}
	}

	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
		return s.filter((DBVar[] d) -> pred.test(d[colNum]));
	}
}
