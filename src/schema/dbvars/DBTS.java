package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;
import java.util.Objects;

public class DBTS extends DBVar {
	private static final long serialVersionUID = 1L;

	public final long val;

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

	@Override
	public String toString() {
		return (isNull() ? "" : Long.toUnsignedString(val));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DBTS)) return false;
		DBTS dbts = (DBTS) o;
		return val == dbts.val;
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
