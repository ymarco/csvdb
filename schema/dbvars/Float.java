package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class Float extends DBVar {
	public double val;

	public Float(String s) throws NumberFormatException {
		this(Double.parseDouble(s.equals("") ? Double.toString(NULL.val) : s));
	}

	Float(double val) {
		this.val = val;
	}

	public static final Float NULL = new Float(Double.NaN);

	@Override
	public DBVar getNull() {
		return NULL;
	}

	@Override
	public Comparator<DBVar> comparator() {
		return comparator;
	}

	public static final Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((Float) dbVar).val);

	@Override
	public Comparator<DBVar> getNegComparator() {
		return negComparator;
	}

	public static final Comparator<DBVar> negComparator = comparator.reversed();

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public boolean isNull() {
		return this.val == NULL.val;
	}

	private static final DBVar.Type type = Type.FLOAT;

}
