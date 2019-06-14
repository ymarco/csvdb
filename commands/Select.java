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

import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.WhereSTREAMS;
import schema.Column2;
import schema.DBVar;
import schema.Schema;
import utils.FilesUtils;


public class Select implements Command {
	private String tableName;
	private String fromTableName;
	private Expression[] expressions;
	private WhereSTREAMS where; //don't work now
	private GroupBy groupBy; //don't work now
	private OrderBy orderBy; //don't work now

	public Select(String tableName, String fromTableName, Expression[] expressions, WhereSTREAMS where, GroupBy groupBy, OrderBy orderBy) {
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

		Column2[] columns = new Column2[expressions.length];
		for (int i = 0; i < columns.length; i++) {
			Column2 column = fromSchema.getColumn(expressions[i].fieldName);
			columns[i] = new Column2(column.type, expressions[i].asName, null /*for compile*/);
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
				if (fromSchema.getColumnType(i) == DBVar.Type.VARCHAR)
					inFiles[i] = new BufferedReader(new FileReader(fromSchema.getTablePath() + "\\" + fromSchema.getColumnName(i) + ".onym"));
				else
					inFilesBin[i] = new DataInputStream(new FileInputStream(fromSchema.getTablePath() + "\\" + fromSchema.getColumnName(i) + ".onym"));
			}

			for (int i = 0; i < outFiles.length; i++) {
				if (schema.getColumnType(i) == DBVar.Type.VARCHAR)
					outFiles[i] = new BufferedWriter(new FileWriter(schema.getTablePath() + "\\" + schema.getColumnName(i) + ".onym"));
				else
					outFilesBin[i] = new DataOutputStream(new FileOutputStream(schema.getTablePath() + "\\" + schema.getColumnName(i) + ".onym"));
			}

			//fill
			int lineCount = 0;
			DBVar[] line = new DBVar[fromSchema.getColumnsCount()];
			for (int i = 0; i < fromSchema.getColumnsCount(); i++) {
				line[i] = new DBVar();
			}
			for (int i = 0; i < fromSchema.getLinesCount(); i++) {
				//read (to line)
				for (int j = 0; j < line.length; j++) {
					switch (fromSchema.getColumnType(j)) {
						case INT:
							line[j].i = inFilesBin[j].readLong();
							break;
						case TS:
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
				if (where != null && !where.pred.check(line[whereInd]))
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
							case TS:
								outFilesBin[fieldIndex].writeLong(line[fromFieldIndex].ts);
								break;
							case FLOAT:
								outFilesBin[fieldIndex].writeDouble(line[fromFieldIndex].f);
								break;
							case VARCHAR:
								outFiles[fieldIndex].write(FilesUtils.endoceStringForWriting(line[fromFieldIndex].s));
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


	public static class Expression {
		public enum AggFuncs {NOTHING, MIN, MAX, AVG, SUM, COUNT};
		
		public String fieldName;
		public String asName;
		public AggFuncs aggFunc;
		
		public Expression(String fieldName) {
			this(fieldName, AggFuncs.NOTHING);
		}
		
		public Expression(String fieldName, AggFuncs aggFunc) {
			this(fieldName, fieldName, aggFunc);
		}
		
		public Expression(String fieldName, String asName) {
			this(fieldName, asName, AggFuncs.NOTHING);
		}
		
		public Expression(String fieldName, String asName, AggFuncs aggFunc) {
			this.fieldName = fieldName;
			this.asName = asName;
			this.aggFunc = aggFunc;
		}
	}


}