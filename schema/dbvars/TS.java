package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class TS extends DBVar {
	long val;

	public TS(long val) {
		this.val = val;
	}

	public TS(String s) throws NumberFormatException {
		this(Long.parseUnsignedLong(s));
	}

	static final TS NULL = new TS(Long.MIN_VALUE);

	@Override
	public DBVar getNull() {
		return NULL;
	}

	private static Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((TS) dbVar).val);

	@Override
	public Comparator<DBVar> comparator() {
		return comparator;
	}
	@Override
	public Comparator<DBVar> getNegComparator() {
		return negComparator;
	}

	private static Comparator<DBVar> negComparator = comparator.reversed();

	@Override
	public Type getType() {
		return type;
	}

	private static DBVar.Type type = Type.TS;
}
