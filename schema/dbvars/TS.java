package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class TS extends DBVar {
	long val;

	public TS(long val) {
		this.val = val;
	}

	public TS(String s) throws NumberFormatException {
		this(Long.parseUnsignedLong(s.equals("") ? Long.toUnsignedString(NULL.val) : s));
	}

	public static final TS NULL = new TS(Long.MIN_VALUE);

	@Override
	public DBVar getNull() {
		return NULL;
	}

	public static final Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((TS) dbVar).val);

	@Override
	public Comparator<DBVar> comparator() {
		return comparator;
	}

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

	public static final DBVar.Type type = Type.TS;
}
