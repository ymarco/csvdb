package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class Varchar extends DBVar {
	String val;

	public Varchar(String val) {
		this.val = val;
	}

	private static final Varchar NULL = new Varchar("");

	@Override
	public DBVar getNull() {
		return NULL;
	}

	private static Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((Varchar) dbVar).val);

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

	private static DBVar.Type type = Type.VARCHAR;
}
