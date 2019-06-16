package commands;


import java.io.IOException;

import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.Where;
import schema.Column2;
import schema.DBVar;
import schema.Schema;

public class CreateAsSelect implements Command {
	private Command create;
	private Select select;
	private String tableName;

	public CreateAsSelect(String tableName, Column2[] columns, String fromTableName, Select.Expression[] expressions, Where where,
	                      GroupBy groupBy, OrderBy orderBy) {
		if (Schema.HaveSchema(tableName)) {
			create = new Create(fromTableName, false, columns);
			select = new Select(tableName, fromTableName, expressions, where, groupBy, orderBy);
		} else {
			throw new RuntimeException("you tried to 'create as select' an existing table");
		}
		this.tableName = tableName;
	}

	public void run() {
		create.run();
		select.run();
		try {
			Load.writeTable(select.getNewTableStream().toArray(DBVar[][]::new), Schema.GetSchema(tableName).getTableFilePath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
