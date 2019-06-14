package commands;

import commands.Select.Expression;
import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.WhereSTREAMS;
import schema.Column2;
import schema.Schema;

public class CreateAsSelect implements Command {
	Command create;
	Command select;

	public CreateAsSelect(String tableName, Column2[] columns, String fromTableName, Expression[] expressions, WhereSTREAMS where, GroupBy groupBy, OrderBy orderBy) {
		if (Schema.HaveSchema(tableName)) {
			create = new Create(fromTableName, false, columns);
			select = new Select(tableName, fromTableName, expressions, where, groupBy, orderBy);
		} else {
			throw new RuntimeException("you tried to 'create as select' an existing table");
		}
	}

	public void run() {
		create.run();
		select.run();
	}
}
