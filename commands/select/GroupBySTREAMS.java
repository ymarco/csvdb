package commands.select;

import schema.DBVar;

import java.util.stream.Stream;

public class GroupBySTREAMS implements Statement {
	public String[] fieldsName;
	public Where2 having;

	public GroupBySTREAMS(String[] fieldsName, Where2 having) {
		this.fieldsName = fieldsName;
		this.having = having;
	}

	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
        return s; //TODO
	}
}
