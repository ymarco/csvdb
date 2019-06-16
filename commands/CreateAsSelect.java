package commands;


import java.io.IOException;

import commands.Select.Expression;
import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.Where;
import schema.Column;
import schema.DBVar;
import schema.Schema;

public class CreateAsSelect implements Command {
	private Command create;
	private Select select;
	private String tableName;

<<<<<<< HEAD
	public CreateAsSelect(String tableName, Column2[] columns, String fromTableName, Select.Expression[] expressions, Where where,
			GroupBy groupBy, OrderBy orderBy) {
=======
	public CreateAsSelect(String tableName, Column[] columns, String fromTableName, Select.Expression[] expressions, Where where,
	                      GroupBy groupBy, OrderBy orderBy) {
>>>>>>> branch 'master' of https://ofek2608@bitbucket.org/csvdb_/csvdb.git
		if (Schema.HaveSchema(tableName)) {
			create = new Create(fromTableName, false, columns);
			select = new Select(tableName, fromTableName, expressions, where, groupBy, orderBy);
		} else {
			throw new RuntimeException("you tried to 'create as select' an existing table");
		}
		this.tableName = tableName;
	}

	public CreateAsSelect(String tableName, String fromTableName, Select.Expression[] expressions, Where where) {
		if (Schema.HaveSchema(fromTableName)) {
			Schema fromSchema = Schema.GetSchema(fromTableName);
			Column2[] columns = new Column2[expressions.length];
			for (int i = 0; i < expressions.length; i++) {
				Expression exp = expressions[i];
				columns[i] = new Column2(fromSchema.getColumnType(exp.fieldName), exp.asName, null);
			}
			
			create = new Create(tableName, false, columns);
			select = new Select(tableName, fromTableName, expressions, where, null, null);
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
