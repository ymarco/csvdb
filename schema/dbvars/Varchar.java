package schema.dbvars;

import schema.DBVar;

import java.util.Comparator;

public class Varchar extends DBVar {
	String val;

	public Varchar(String val) {
		this.val = val;
	}

	public static final Varchar NULL = new Varchar("");

	@Override
	public DBVar getNull() {
		return NULL;
	}

	public static final Comparator<DBVar> comparator = Comparator.comparing(dbVar -> ((Varchar) dbVar).val);

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
}
