package commands.select;

import schema.DBVar;
import schema.Schema;
import schema.dbvars.Int;
import schema.dbvars.TS;
import schema.dbvars.Varchar;
import schema.dbvars.Float;

import java.util.Comparator;
import java.util.stream.Stream;

public class OrderBySTREAMS implements Statement {
	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
		return s.sorted(comparator);
	}


	private final int colNum;
	private final Comparator<DBVar[]> comparator;
	private final boolean isDesc;

	public OrderBySTREAMS(String tableName, int colNum) {
		this(tableName, colNum, false);
	}

	public OrderBySTREAMS(String tableName, int colNum, boolean isDesc) {
		Comparator<DBVar> basicComparator;
		Schema schema = Schema.GetSchema(tableName);
		this.colNum = colNum;
		this.isDesc = isDesc;
		// create basic comparator
		switch (schema.getColumn(colNum).type) {
			case INT:
				basicComparator = Int.comparator;
				break;
			case FLOAT:
				basicComparator = Float.comparator;
				break;
			case VARCHAR:
				basicComparator = Varchar.comparator;
				break;
			case TS:
				basicComparator = TS.comparator;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + schema.getColumn(colNum).type);
		}

		if (isDesc) {
			basicComparator = basicComparator.reversed();
		}

		Comparator<DBVar> finalBasicComparator = basicComparator;
		basicComparator = (v1, v2) -> (v1.isNull()? -1 : finalBasicComparator.compare(v1,v2));

		Comparator<DBVar> finalBasicComparator1 = basicComparator;
		comparator = (a1, a2) -> finalBasicComparator1.compare(a1[colNum], a2[colNum]);
	}


}
