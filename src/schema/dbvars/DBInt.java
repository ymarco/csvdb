package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;
import java.util.Objects;

public class DBInt extends DBVar {
	private static final long serialVersionUID = 1L;

	public final long val;

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
		return (isNull() ? "" : Long.toString(val));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DBInt)) return false;
		DBInt dbInt = (DBInt) o;
		return val == dbInt.val;
	}

	@Override
	public int hashCode() {
		return Objects.hash(val);
	}

	@Override
	public int compareTo(DBVar dbVar) {
		return comparator.compare(this, dbVar);
	}
}
