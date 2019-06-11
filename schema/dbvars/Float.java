package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class Float extends DBVar {
	public double val;

	public Float(String s) throws NumberFormatException {
		this(Double.parseDouble(s));
	}

	Float(double val) {
		this.val = val;
	}

	private static final Float NULL = new Float(Double.NaN);

	@Override
	public DBVar getNull() {
		return NULL;
	}

	@Override
	public Comparator<DBVar> comparator() {
		return comparator;
	}

	private static Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((Float) dbVar).val);

	@Override
	public Comparator<DBVar> getNegComparator() {
		return negComparator;
	}

	private static Comparator<DBVar> negComparator = comparator.reversed();

	@Override
	public Type getType() {
		return type;
	}

	private static DBVar.Type type = Type.FLOAT;

}
