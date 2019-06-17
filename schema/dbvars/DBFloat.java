package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;
import java.util.Objects;

public class DBFloat extends DBVar {
	private static final long serialVersionUID = 1L;
	
	public final double val;

	public DBFloat(String s) throws NumberFormatException {
		this(Double.parseDouble(s.equals("") ? Double.toString(NULL.val) : s.trim()));
	}

	public DBFloat(double val) {
		this.val = val;
	}

	public static final DBFloat NULL = new DBFloat(Double.NaN);

	@Override
	public DBVar getNull() {
		return NULL;
	}

	@Override
	public Comparator<DBVar> comparator() {
		return comparator;
	}

	public static final Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((DBFloat) dbVar).val);

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

	@Override
	public String toString() {
		return (isNull() ? "" : Double.toString(val));
	}

	private static final DBVar.Type type = Type.FLOAT;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DBFloat)) return false;
		DBFloat dbFloat = (DBFloat) o;
		return Double.compare(dbFloat.val, val) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(val);
	}
}
