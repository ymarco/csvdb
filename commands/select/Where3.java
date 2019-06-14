package commands.select;

import java.util.Comparator;

import schema.DBVar;
import schema.Schema;

//classes
public class Where3 {
	public interface Filter {
		boolean check(DBVar var);
	}
	
	public final String fieldName;
	public final String operator;
	public DBVar constant;
	private Comparator<DBVar> comparator;
	private Filter filter;

	public Where3(Schema schema, String fieldName, String operator, String constant) {
		this.fieldName= fieldName;
		this.operator = operator;
		this.constant = parseConstant(constant, schema.getColumnType(fieldName));
		//TODO add checks for operators: is, is not
		
		//Comparator<DBVar> comparator = null; 
		switch (this.constant.varType) {
		case INT:
			comparator = (x,y) -> Long.compare(x.i, y.i);
			break;
		case FLOAT:
			comparator = (x,y) -> Double.compare(x.f, y.f);
			break;
		case TIMESTAMP:
			comparator = (x,y) -> Long.compareUnsigned(x.ts, y.ts);
			break;
		case VARCHAR:
			comparator = (x,y) -> x.s.compareTo(y.s);
			break;
		}
		switch (operator) {
		case "<":
			filter = (x) -> comparator.compare(x, this.constant) < 0;
			break;
		case "<=":
			filter = (x) -> comparator.compare(x, this.constant) <= 0;
			break;
		case ">":
			filter = (x) -> comparator.compare(x, this.constant) > 0;
			break;
		case ">=":
			filter = (x) -> comparator.compare(x, this.constant) >= 0;
			break;
		case "==":
			filter = (x) -> comparator.compare(x, this.constant) == 0;
			break;
		case "<>":
			filter = (x) -> comparator.compare(x, this.constant) != 0;
			break;
		}
	}
	
	public boolean filter(DBVar var) {
		return filter.check(var);
	}
	
	private static DBVar parseConstant(String constant, DBVar.Type type) {
		DBVar res = new DBVar();
		res.varType = type;
		try {
			switch (type) {
			case INT:
				res.i = constant.equals("null") ? DBVar.NULL_INT : Long.parseLong(constant);
				return res;
			case FLOAT:
				res.f = constant.equals("null") ? DBVar.NULL_FLOAT : Double.parseDouble(constant);
				return res;
			case TS:
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
