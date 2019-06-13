package commands.select;

import schema.DBVar;

import java.util.stream.Stream;

public class OrderBySTREAMS implements Statement {
	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
        return s; //TODO
	}

	public enum SortType {ASC,DESC};

	public final String outputFieldName;
	public final SortType sortType;

	public OrderBySTREAMS(String outputFieldName, SortType sortType) {
		this.outputFieldName = outputFieldName;
		this.sortType = sortType;
	}


}
