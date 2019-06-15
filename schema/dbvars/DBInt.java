package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class DBInt extends DBVar {
	long val;

	public DBInt(long val) {
		this.val = val;
	}

	public DBInt(String s) throws NumberFormatException {
		this(s.equals("") ? NULL.val : Long.parseLong(s.trim()));
	}

	public static final DBInt NULL = new DBInt(Long.MIN_VALUE);

	@Override
	public DBVar getNull() {
		return NULL;
	}

	public static final Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((DBInt) dbVar).val);

	@Override
	public Comparator<DBVar> comparator() {
		return comparator;
	}

	@Override
	public Comparator<DBVar> getNegComparator() {
		return negComparator;
	}

	private static final Comparator<DBVar> negComparator = comparator.reversed();


	@Override
	public Type getType() {
		return type;
	}

	@Override
	public boolean isNull() {
		return this.val == NULL.val;
	}

	public static final DBVar.Type type = Type.INT;

	@Override
	public String toString() {
		return (isNull() ? "null" : Long.toString(val));
	}
}
