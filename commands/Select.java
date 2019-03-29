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

	public Select(String tableName, String fromTableName, Expression[] expressions, Condition where, GroupBy groupBy, OrderBy orderBy) {
		this.tableName = tableName;
		this.fromTableName = fromTableName;
		this.expressions = expressions;
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
		for (int i = 0; i < columns.length; i++) {
			Column column = fromSchema.getColumn(expressions[i].fieldName); 
			columns[i] = new Column(column.type, expressions[i].asName);
		}
		new Create(tableName, false, columns).run();
	}

	private void fillTable() {
		try {
			//init
			Schema fromSchema = Schema.GetSchema(fromTableName);
			Schema schema = Schema.GetSchema(tableName);
			int whereInd = -1;
			if (where != null)
				whereInd = fromSchema.getColumnIndex(expressions[schema.getColumnIndex(where.fieldName)].fieldName);
			
			//open
			BufferedReader[] inFiles = new BufferedReader[fromSchema.getColumnsCount()];
			DataInputStream[] inFilesBin = new DataInputStream[fromSchema.getColumnsCount()];
			BufferedWriter[] outFiles = new BufferedWriter[schema.getColumnsCount()];
			DataOutputStream[] outFilesBin = new DataOutputStream[schema.getColumnsCount()];

			for (int i = 0; i < inFiles.length; i++) {
				if (fromSchema.getColumnType(i) == VarType.VARCHAR)
					inFiles[i] = new BufferedReader(new FileReader(fromSchema.getTablePath() + "\\" + fromSchema.getColumnName(i) + ".onym"));
				else
					inFilesBin[i] = new DataInputStream(new FileInputStream(fromSchema.getTablePath() + "\\" + fromSchema.getColumnName(i) + ".onym"));
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
				//read (to line)
				Object[] line = new Object[fromSchema.getColumnsCount()];
				for (int j = 0; j < line.length; j++) {
					switch (fromSchema.getColumnType(j)) {
					case INT:
						line[j] = inFilesBin[j].readLong();
						break;
					case TIMESTAMP:
						line[j] = inFilesBin[j].readLong();
						break;
					case FLOAT:
						line[j] = inFilesBin[j].readFloat();
						break;
					case VARCHAR:
						line[j] = inFiles[j].readLine();
						break;
					}
				}
				
				//check where
				if (where != null && !where.isTrue(line[whereInd]))
					continue;
				
				//write
				for (int j = 0; j < expressions.length; j++) {
					Expression expression = expressions[j];
					int fromFieldIndex = fromSchema.getColumnIndex(expression.fieldName);
					int fieldIndex = schema.getColumnIndex(expression.asName);
					try {
						switch (schema.getColumnType(j)) {
						case INT:
							outFilesBin[fieldIndex].writeLong((long) line[fromFieldIndex]);
							outFilesBin[fieldIndex].flush();
							break;
						case TIMESTAMP:
							outFilesBin[fieldIndex].writeLong((long) line[fromFieldIndex]);
							outFilesBin[fieldIndex].flush();
							break;
						case FLOAT:
							outFilesBin[fieldIndex].writeFloat((float) line[fromFieldIndex]);
							outFilesBin[fieldIndex].flush();
							break;
						case VARCHAR:
							outFiles[fieldIndex].write((String) line[fromFieldIndex] + "\n");
							outFiles[fieldIndex].flush();
							break;
						}
					} catch (Exception e) {
						closeAll(schema, fromSchema, inFiles, inFilesBin, outFiles, outFilesBin);
						throw new RuntimeException("^^^Select Exeption^^^");
					}
					lineCount++;
				}

				schema.setLineCount(lineCount);



				
			}
			closeAll(schema, fromSchema, inFiles, inFilesBin, outFiles, outFilesBin);
		} catch (IOException e) {
			throw new RuntimeException("^^^Select Exeption^^^");
		}
	}

	private void closeAll(Schema schema, Schema fromSchema, BufferedReader[] inFiles, DataInputStream[] inFilesBin,
			BufferedWriter[] outFiles, DataOutputStream[] outFilesBin) throws IOException {
		for (int i = 0; i < outFiles.length; i++) {
			if (fromSchema.getColumnType(i) == VarType.VARCHAR)
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
		
		// don't work on Strings
		public boolean isTrue(Object field) {
			if (field instanceof Long)
				return op.isTrue((long) field, (long) constant);
			if (field instanceof Float)
				return op.isTrue((float) field, (float) constant);
			if (constant == "NULL")
				return op.isTrueBNull((String) field);
			return op.isTrue((String) field, (String) constant);
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
		
		public boolean isTrue(String a, String b) {
			switch (this) {
			case eq:
				return a.equals(b);
			case notEq:
				return !a.equals(b);
			default:
				return false;
			}
		}
		
		public boolean isTrueBNull(String a) {
			switch (this) {
			case eq:
				return a == null;
			case notEq:
				return a != null;
			default:
				return false;
			}
		}
	}
}