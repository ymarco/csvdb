package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class Int extends DBVar {
	long val;

	public Int(long val) {
		this.val = val;
	}

	public Int(String s) throws NumberFormatException {
		this(Long.parseLong(s));
	}

	public static final Int NULL = new Int(Long.MIN_VALUE);

	@Override
	public DBVar getNull() {
		return NULL;
	}

	public static final Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((Int) dbVar).val);

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
}
