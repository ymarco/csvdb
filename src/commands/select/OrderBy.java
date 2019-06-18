package commands.select;

import schema.DBVar;

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

	public OrderBy(int[] colNum) {
		this(colNum, new boolean[colNum.length]);
	}

	public OrderBy(int[] colNums, boolean[] isDesc) {

		List<Comparator<DBVar>> basicComparators = new ArrayList<>(colNums.length);
		for (int i = 0; i < colNums.length; i++) {
			basicComparators.add(getBasicComparator(isDesc[i]));
		}

		comparator = (a1, a2) -> {
			for (int i = 0; i < colNums.length; i++) {
				int colNum = colNums[i];
				int ret = basicComparators.get(i).compare(a1[colNum], a2[colNum]);
				if (ret != 0)
					return ret;
			}
			return 0;
		};
	}

	private static Comparator<DBVar> getBasicComparator(boolean isDesc) {
		Comparator<DBVar> basicComparator = DBVar::compareTo;
		if (isDesc) {
			basicComparator = basicComparator.reversed();
		}
		// adding correct null handling
		Comparator<DBVar> basicComparatorCopy = basicComparator;
		basicComparator = (v1, v2) -> (v1.isNull() ? -1 : basicComparatorCopy.compare(v1, v2));
		return basicComparator;
	}
}
