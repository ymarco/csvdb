package commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import schema.Column;
import schema.Schema;
import schema.VarType;

public class Select implements Command {
	private String tableName;
	private String fromTableName;
	private Expression[] expressions;
	private Condition where; //don't work now
	private GroupBy groupBy; //don't work now
	private OrderBy orderBy; //don't work now

	public Select(String tableName, Expression[] expression, Condition where, GroupBy groupBy, OrderBy orderBy) {
		this.tableName = tableName;
		this.expressions = expression;
		this.where = where;
		this.groupBy = groupBy;
		this.orderBy = orderBy;
	}

	public void run() {
		if (Schema.HaveSchema(tableName))
			throw new RuntimeException("you tried to select an existing table");

		createTable();
		fillTable();
	}

	private void createTable() {
		Schema fromSchema = Schema.GetSchema(fromTableName);
		if (expressions == null) {
			expressions = new Expression[fromSchema.getLinesCount()];
			for (int i = 0; i < expressions.length; i++)
				expressions[i] = new Expression(fromSchema.getColumnName(i));
		}

		Column[] columns = new Column[expressions.length];
		for (int i = 0; i < columns.length; i++)
			columns[i] = fromSchema.getColumn(expressions[i].fieldName);
		new Create(tableName, false, columns).run();
	}

	private void fillTable() {
		try {
			Schema fromSchema = Schema.GetSchema(fromTableName);
			Schema schema = Schema.GetSchema(tableName);

			//open
			BufferedReader[] inFiles = new BufferedReader[fromSchema.getColumnsCount()];
			DataInputStream[] inFilesBin = new DataInputStream[fromSchema.getColumnsCount()];
			BufferedWriter[] outFiles = new BufferedWriter[schema.getColumnsCount()];
			DataOutputStream[] outFilesBin = new DataOutputStream[schema.getColumnsCount()];

			for (int i = 0; i < outFiles.length; i++) {
				if (schema.getColumnType(i) == VarType.VARCHAR)
					inFiles[i] = new BufferedReader(new FileReader(schema.getTablePath() + "\\" + schema.getColumnName(i) + ".onym"));
				else
					inFilesBin[i] = new DataInputStream(new FileInputStream(schema.getTablePath() + "\\" + schema.getColumnName(i) + ".onym"));
			}

			for (int i = 0; i < outFiles.length; i++) {
				if (schema.getColumnType(i) == VarType.VARCHAR)
					outFiles[i] = new BufferedWriter(new FileWriter(schema.getTablePath() + "\\" + schema.getColumnName(i) + ".onym"));
				else
					outFilesBin[i] = new DataOutputStream(new FileOutputStream(schema.getTablePath() + "\\" + schema.getColumnName(i) + ".onym"));
			}

			//fill
			int lineCount = 0;
			for (int i = 0; i < fromSchema.getLinesCount(); i++) {
				for (Expression expression : expressions) {
					
					int columnInd = schema.getColumnIndex(expression.asName);
					int fromColumnInd = fromSchema.getColumnIndex(expression.fieldName);
					try {
						switch (schema.getColumnType(i)) {
						case INT:
							outFilesBin[columnInd].writeLong(inFilesBin[fromColumnInd].readLong());
							outFilesBin[columnInd].flush();
							break;
						case TIMESTAMP:
							outFilesBin[columnInd].writeLong(inFilesBin[fromColumnInd].readLong());
							outFilesBin[columnInd].flush();
							break;
						case FLOAT:
							outFilesBin[columnInd].writeFloat(inFilesBin[fromColumnInd].readFloat());
							outFilesBin[columnInd].flush();
							break;
						case VARCHAR:
							outFiles[columnInd].write(inFiles[fromColumnInd].readLine() + "\n");
							outFiles[columnInd].flush();
							break;
						}
					} catch (Exception e) {
						closeAll(schema, inFiles, inFilesBin, outFiles, outFilesBin);
						e.printStackTrace();
						throw new RuntimeException("^^^Select Exeption^^^");
					}
				}

				schema.setLineCount(lineCount);



				closeAll(schema, inFiles, inFilesBin, outFiles, outFilesBin);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("^^^Select Exeption^^^");
		}
	}

	private void closeAll(Schema schema, BufferedReader[] inFiles, DataInputStream[] inFilesBin,
			BufferedWriter[] outFiles, DataOutputStream[] outFilesBin) throws IOException {
		for (int i = 0; i < outFiles.length; i++) {
			if (schema.getColumnType(i) == VarType.VARCHAR)
				inFiles[i].close();
			else
				inFilesBin[i].close();
		}

		for (int i = 0; i < outFiles.length; i++) {
			if (schema.getColumnType(i) == VarType.VARCHAR)
				outFiles[i].close();
			else
				outFilesBin[i].close();
		}
	}
	
	
	
	//classes
	public static class Condition {
		public String fieldName;
		public Operator op;
		public Object constant;
		
		public Condition(String fieldName, Operator op, Object constant) {
			this.fieldName = fieldName;
			this.op = op;
			this.constant = constant;
		}
		
		public boolean isTrue(long field) { // so the constant is long
			return op.isTrue(field, (long) constant);
		}
		
		public boolean isTrue(float field) { // so the constant is float
			return op.isTrue(field, (float) constant);
		}
	}
	
	public static class Expression {
		public String fieldName;
		public String asName;
		
		public Expression(String fieldName, String asName) {
			this.fieldName = fieldName;
			this.asName = asName;
		}
		
		public Expression(String fieldName) {
			this(fieldName, fieldName);
		}
	}
	
	public static class GroupBy {
		public String[] fieldsName;
		public Condition having;
		
		public GroupBy(String[] fieldsName, Condition having) {
			this.fieldsName = fieldsName;
			this.having = having;
		}
	}
	
	public static class OrderBy {

	}
	
	
	public static enum Operator {
		lit,
		litEq,
		eq,
		bigEq,
		big,
		notEq;

		public static Operator Get(String op) {
			switch (op) {
			case "<":
				return lit;
			case "<=":
				return litEq;
			case "=":
				return eq;
			case ">=":
				return bigEq;
			case ">":
				return big;
			case "<>":
				return notEq;
			default:
				return null;
			}
		}
		
		public boolean isTrue(float a, float b) {
			switch (this) {
			case lit:
				return a < b;
			case litEq:
				return a <= b;
			case eq:
				return a == b;
			case bigEq:
				return a >= b;
			case big:
				return a > b;
			case notEq:
				return a != b;
			default:
				return false;
			}
		}
	}
}