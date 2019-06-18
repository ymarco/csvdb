package commands;


import commands.select.SelectExpression;
import commands.select.Where;
import schema.Column;
import schema.Schema;

public class CreateAsSelect implements Command {
	private Command create;
	private Command drop;
	private Select select;

	public CreateAsSelect(String tableName, String srcTableName, SelectExpression[] expressions, Where where) {
		Schema srcSchema = Schema.GetSchema(srcTableName);
		Column[] columns = new Column[expressions.length];
		for (int i = 0; i < expressions.length; i++) {
			SelectExpression exp = expressions[i];
			columns[i] = new Column(srcSchema.getColumnType(exp.fieldName), exp.asName);
		}

		drop = new Drop(tableName, true);
		create = new Create(tableName, false, columns);
		select = new Select(tableName, srcTableName, expressions, where, null, null, Select.Mode.CREATE_NEW_TABLE);
	}

	public void run() {
		drop.run();
		create.run();
		select.run();
	}
}
