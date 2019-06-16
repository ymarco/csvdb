package commands.select;

import schema.DBVar;
import schema.Schema;

import java.util.stream.Stream;

public class GroupBy implements Statement {
	public String[] fieldsToGroupBy;
	public Where having;
	public Schema schema;

	public GroupBy(String[] fieldsToGroupBy, Where having) {
		this.fieldsToGroupBy = fieldsToGroupBy;
		this.having = having;
	}

	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
        return s; //TODO
	}
}
