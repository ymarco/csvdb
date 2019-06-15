package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class DBTS extends DBVar {
	long val;

	public DBTS(long val) {
		this.val = val;
	}

	public DBTS(String s) throws NumberFormatException {
		this(Long.parseUnsignedLong(s.equals("") ? Long.toUnsignedString(NULL.val) : s.trim()));
	}

	public static final DBTS NULL = new DBTS(Long.MIN_VALUE);

	@Override
	public DBVar getNull() {
		return NULL;
	}

	public static final Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((DBTS) dbVar).val);

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
