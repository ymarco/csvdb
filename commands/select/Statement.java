package commands.select;

import schema.DBVar;

import java.util.stream.Stream;

public interface Statement {
	Stream<DBVar[]> apply(Stream<DBVar[]> s);
	Statement emptyStatement = s -> s;
}
