package commands;


import java.io.IOException;

import commands.Select.Expression;
import commands.select.Where;
import schema.Column;
import schema.DBVar;
import schema.Schema;

public class CreateAsSelect implements Command {
	private Command create;
	private Select select;
	private String tableName;

	public CreateAsSelect(String tableName, String fromTableName, Select.Expression[] expressions, Where where) {
		if (Schema.HaveSchema(fromTableName)) {
			Schema fromSchema = Schema.GetSchema(fromTableName);
			Column[] columns = new Column[expressions.length];
			for (int i = 0; i < expressions.length; i++) {
				Expression exp = expressions[i];
				columns[i] = new Column(fromSchema.getColumnType(exp.fieldName), exp.asName);
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
