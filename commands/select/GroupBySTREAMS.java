package commands.select;

import schema.DBVar;

import java.util.stream.Stream;

public class GroupBySTREAMS implements Statement {
	public String[] fieldsName;
	public WhereSTREAMS having;

	public GroupBySTREAMS(String[] fieldsName, WhereSTREAMS having) {
		this.fieldsName = fieldsName;
		this.having = having;
	}

	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
        return s; //TODO
	}
}
