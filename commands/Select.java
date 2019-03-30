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
import schema.DBVar;
import schema.Schema;
import schema.VarType;
import utils.FilesUtils;

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
			DBVar[] line = new DBVar[fromSchema.getColumnsCount()];
			for (int i = 0; i < fromSchema.getLinesCount(); i++) {
				//read (to line)
				for (int j = 0; j < line.length; j++) {
					switch (fromSchema.getColumnType(j)) {
						case INT:
							line[j].i = inFilesBin[j].readLong();
							break;
						case TIMESTAMP:
							line[j].ts = inFilesBin[j].readLong();
							break;
						case FLOAT:
							line[j].f = inFilesBin[j].readFloat();
							break;
						case VARCHAR:
							line[j].s = inFiles[j].readLine();
							break;
					}
				}

				//check where
				if (where != null && !where.test.check(line[whereInd]))
					continue;

				//write
				for (int j = 0; j < expressions.length; j++) {
					Expression expression = expressions[j];
					int fromFieldIndex = fromSchema.getColumnIndex(expression.fieldName);
					int fieldIndex = schema.getColumnIndex(expression.asName);
					try {
						switch (schema.getColumnType(j)) {
							case INT:
								outFilesBin[fieldIndex].writeLong(line[fromFieldIndex].i);
								break;
							case TIMESTAMP:
								outFilesBin[fieldIndex].writeLong(line[fromFieldIndex].ts);
								break;
							case FLOAT:
								outFilesBin[fieldIndex].writeDouble(line[fromFieldIndex].f);
								break;
							case VARCHAR:
								outFiles[fieldIndex].write(line[fromFieldIndex].s);
								outFiles[fieldIndex].write('\n');
								break;
						}
					} catch (Exception e) {
						FilesUtils.closeAll(inFiles);
						FilesUtils.closeAll(inFilesBin);
						FilesUtils.closeAll(outFiles);
						FilesUtils.closeAll(outFilesBin);
						throw new RuntimeException("you tried to select file to invalid table");
					}
					lineCount++;
				}

				schema.setLineCount(lineCount);
			}
			FilesUtils.closeAll(inFiles);
			FilesUtils.closeAll(inFilesBin);
			FilesUtils.closeAll(outFiles);
			FilesUtils.closeAll(outFilesBin);
		} catch (IOException e) {
			throw new RuntimeException("^^^Select Exeption^^^");
		}
	}


	//classes
	public static class Condition {
		private final Schema schema;
		public final String fieldName;
		private DBVar constant;

		interface TestCondition {
			boolean check(DBVar var);
		}

		public final TestCondition test;

		public Condition(Schema schema, String fieldName, String operator, String constant_) {
			this.schema = schema;
			this.fieldName= fieldName;
			this.constant = parseConstant(constant_);
			VarType vt = schema.getColumnType(fieldName);
			/* now creating the test function*/
			switch (operator) {
				case "<":
					switch (vt) {
						case INT:
							test = (var -> var.i < constant.i);
							break;
						case FLOAT:
							test = (var -> var.f < constant.f);
							break;
						case TIMESTAMP:
							test = (var -> Long.compareUnsigned(var.ts, constant.ts) < 0);
							break;
						case VARCHAR:
							throw new RuntimeException("invalid WHERE:" +
									"WHERE VARCHAR < ...");
						default:
							test = null;
					}
					break;
				case "<=":
					switch (vt) {
						case INT:
							test = (var -> var.i <= constant.i);
							break;
						case FLOAT:
							test = (var -> var.f <= constant.f);
							break;
						case TIMESTAMP:
							test = (var -> Long.compareUnsigned(var.ts, constant.ts) <= 0);
							break;
						case VARCHAR:
							throw new RuntimeException("invalid WHERE:" +
									"WHERE VARCHAR <= ...");
						default:
							test = null;
					}
					break;
				case ">":
					switch (vt) {
						case INT:
							test = (var -> var.i > constant.i);
							break;
						case FLOAT:
							test = (var -> var.f > constant.f);
							break;
						case TIMESTAMP:
							test = (var -> Long.compareUnsigned(var.ts, constant.ts) > 0);
							break;
						case VARCHAR:
							throw new RuntimeException("invalid WHERE:" +
									"WHERE VARCHAR > ...");
						default:
							test = null;
					}
					break;
				case ">=":
					switch (vt) {
						case INT:
							test = (var -> var.i >= constant.i);
							break;
						case FLOAT:
							test = (var -> var.f >= constant.f);
							break;
						case TIMESTAMP:
							test = (var -> Long.compareUnsigned(var.ts, constant.ts) >= 0);
							break;
						case VARCHAR:
							throw new RuntimeException("invalid WHERE:" +
									"WHERE VARCHAR >= ...");
						default:
							test = null;
					}
					break;
				case "<>":
					switch (vt) {
						case INT:
							test = (var -> var.i != constant.i);
							break;
						case FLOAT:
							test = (var -> var.f != constant.f);
							break;
						case TIMESTAMP:
							test = (var -> var.ts != constant.ts);
							break;
						case VARCHAR:
							throw new RuntimeException("invalid WHERE:" +
									"WHERE VARCHAR <> ...");
						default:
							test = null;
					}
					break;
				default:
					throw new RuntimeException("invalid where operator");

			}
		}

		private static DBVar parseConstant(String constant) {
			DBVar res = new DBVar();
			if (constant.equals("none")) {
				res.i = DBVar.NULL_INT;
				res.s = DBVar.NULL_STRING;
				res.f = DBVar.NULL_FLOAT;
				res.ts = DBVar.NULL_TS;
			} else {
				res.i = Long.parseLong(constant);
				res.s = constant;
				res.f = Double.parseDouble(constant);
				res.ts = Long.parseUnsignedLong(constant);
			}
			return res;
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


}