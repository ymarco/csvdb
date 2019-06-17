package commands.select;

import schema.DBVar;
import schema.DBVar.Type;
import schema.Schema;
import schema.dbvars.DBInt;
import schema.dbvars.DBTS;
import schema.dbvars.DBVarchar;
import schema.dbvars.DBFloat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class OrderBy implements Statement {
	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
		return s.sorted(comparator);
	}
	
	private final Comparator<DBVar[]> comparator;
	private final boolean isDesc;

	public OrderBy(String tableName, int[] colNum) {
		this(tableName, colNum, false);
	}

	public OrderBy(String tableName, int[] colNums, boolean isDesc) {
		Schema schema = Schema.GetSchema(tableName);
		this.isDesc = isDesc;
		
		List<Comparator<DBVar>> basicComparators = new ArrayList<Comparator<DBVar>>(colNums.length);
		List<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < colNums.length; i++) { 
			basicComparators.add(getBasicComperator(schema.getColumnType(colNums[i]), colNums[i]));
			indexes.add(i);
		}
		
		comparator = (a1,a2) -> {
			for (int i = 0; i < indexes.size(); i++) {
				int index = indexes.get(i);
				int ret = basicComparators.get(i).compare(a1[index], a2[index]);
				if (ret != 0)
					return ret;
			}
			return 0;
		};
	}
	
	private Comparator<DBVar> getBasicComperator(Type type, int colNum) {
		Comparator<DBVar> basicComparator;
		switch (type) {
		case INT:
			basicComparator = DBInt.comparator;
			break;
		case FLOAT:
			basicComparator = DBFloat.comparator;
			break;
		case VARCHAR:
			basicComparator = DBVarchar.comparator;
			break;
		case TS:
			basicComparator = DBTS.comparator;
			break;
		default:
			throw new IllegalStateException("Unexpected value: " + type);
		}
		
		if (isDesc)
			basicComparator = basicComparator.reversed();
		
		Comparator<DBVar> finalBasicComparator = basicComparator;
		basicComparator = (v1, v2) -> (v1.isNull()? -1 : finalBasicComparator.compare(v1,v2));
		return basicComparator;
	}
}
