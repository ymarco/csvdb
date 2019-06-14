package commands.select;

import schema.DBVar;

import java.util.stream.Stream;

public class GroupBy implements Statement {
	public String[] fieldsName;
	public Where having;

	public GroupBy(String[] fieldsName, Where having) {
		this.fieldsName = fieldsName;
		this.having = having;
	}

	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
        return s; //TODO
	}
}
