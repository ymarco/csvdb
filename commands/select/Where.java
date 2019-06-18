package commands.select;

import schema.DBVar;
import schema.dbvars.DBInt;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

//classes
public class Where implements Statement {
	private final int colNum;
	private DBVar constant;
    private DBVar.Type type;

	public Predicate<DBVar> pred;

	public boolean testRow(DBVar[] row) {
		return pred.test(row[colNum]);
	}

	public Where(DBVar.Type type, int colNum, String operator, String constant_) {
		this.colNum = colNum;
        this.type = type;
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
		if (constant_ == null) {
			this.constant = new DBInt(0);
			return;
		} else if (constant_.equals("null") && type != DBVar.Type.VARCHAR) {
			this.constant = null;
			return;
		}
		try {
			switch (type) {
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
		return s.filter(this::testRow);
	}
}
