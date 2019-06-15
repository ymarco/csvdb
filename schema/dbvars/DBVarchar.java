package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class DBVarchar extends DBVar {
	String val;

	public DBVarchar(String val) {
		this.val = val;
	}

	public static final DBVarchar NULL = new DBVarchar("");

	@Override
	public DBVar getNull() {
		return NULL;
	}

	public static final Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((DBVarchar) dbVar).val);

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
		return this.val.equals(NULL.val);
	}

	public static final DBVar.Type type = Type.VARCHAR;

	@Override
	public String toString() {
		return val;
	}
}
