package commands.select;

import schema.DBVar;
import schema.Schema;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

//classes
public class Where implements Statement {
    public final int colNum;
	private DBVar constant;

	public final Predicate<DBVar> pred;

	public Where(Schema schema, int colNum, String operator, String constant_) {
        this.colNum = colNum;
		DBVar.Type vt = schema.getColumnType(colNum);
		//this.constant = parseConstant(constant_, vt);

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

		/* now creating the pred function*/
		Comparator<DBVar> comparator = this.constant.comparator();
		switch (operator) {
			case "<":
				pred = v -> !v.isNull() && comparator.compare(constant, v) < 0;
				break;
			case "<=":
				pred = v -> !v.isNull() && comparator.compare(constant, v) <= 0;
				break;
			case ">":
				pred = v -> !v.isNull() && comparator.compare(constant, v) > 0;
				break;
			case ">=":
				pred = v -> !v.isNull() && comparator.compare(constant, v) >= 0;
				break;
			case "<>":
				pred = v -> !v.isNull() && comparator.compare(constant, v) != 0;
				break;
			case "is none":
				pred = v -> v.isNull() ;
				break;
			case "is not none":
				pred = v -> !v.isNull();
				break;
			default:
				throw new RuntimeException("invalid where operator");

		}
	}
	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
		return s.filter((DBVar[] d) -> pred.test(d[colNum]));
	}
}
