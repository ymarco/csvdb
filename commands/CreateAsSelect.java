package commands;

import commands.select.Where;
import commands.Select.Expression;
import commands.select.GroupBy;
import commands.select.OrderBy;
import schema.Column;
import schema.Schema;

public class CreateAsSelect implements Command {
	Command create;
	Command select;

	public CreateAsSelect(String tableName, Column[] columns, String fromTableName, Expression[] expressions, Where where, GroupBy groupBy, OrderBy orderBy) {
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
