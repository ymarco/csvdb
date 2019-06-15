package schema;

import java.io.Serializable;
import java.util.Comparator;

public abstract class DBVar implements Serializable {

	public abstract DBVar getNull();

	public abstract Comparator<DBVar> comparator();

	public abstract Comparator<DBVar> getNegComparator();

	public abstract Type getType();

	public abstract boolean isNull();


	public enum Type {
		INT("int"),
		FLOAT("float"),
		VARCHAR("varchar"),
		TS("timestamp");

		private final String toString;

		Type(String toString) {
			this.toString = toString;
		}

		public String toString() {
			return toString;
		}

		public static Type toVarType(String s) {
			switch (s.toLowerCase()) {
				case "int":
					return INT;
				case "varchar":
					return VARCHAR;
				case "float":
					return FLOAT;
				case "timestamp":
					return TS;
			}
			return null;
		}
	}
}
