package commands.select;

import commands.Select3.Expression;
import commands.Select3.Expression.AggFuncs;
import exceptions.CsvdbException;
import schema.Column2;
import schema.DBVar;
import schema.Schema;
import schema.VarType;

public class LoadedTable {
	LoadedColumn[] columns;
	Schema schema;
	int rowsCount;
	
	public LoadedTable(String tableName, Expression[] expressions) {
		//TODO if there are 2 columns with the same column
		
		if (!Schema.HaveSchema(tableName))
			throw new CsvdbException("you tried to select on unexisting table");
		Schema oldSchema = Schema.GetSchema(tableName);
		rowsCount = oldSchema.getLinesCount();
		Column2[] schemaColumns = new Column2[expressions.length];
		
		LoadedColumn[] columns = new LoadedColumn[expressions.length];
		for (int i = 0; i < columns.length; i++) {
			Expression expression = expressions[i];
			
			int colInd = oldSchema.getColumnIndex(expression.fieldName);
			String filePath = oldSchema.getColumnPath(colInd);
			VarType colType = oldSchema.getColumnType(colInd);
			columns[i] = new LoadedColumn(filePath, colType, expression.aggFunc);
			
			schemaColumns[i] = new Column2(colType, expression.asName, filePath);
		}
		Schema.AddSchema(tableName, schemaColumns);
		this.schema = Schema.GetSchema(tableName);
	}
	
	private LoadedColumn getColumnByName(String columnName) {
		return columns[schema.getColumnIndex(columnName)];
	}
	
	public void where(String columnName, Where3 where) {
		boolean[] whereIndexes = getColumnByName(columnName).getWhereIndexes(where);
		int newRowsCount = 0;
		for (boolean index : whereIndexes)
			if (index)
				newRowsCount++;
		for (LoadedColumn column : columns)
			column.applayWhereIndexes(whereIndexes, newRowsCount);
		rowsCount = newRowsCount;
	}
	
	public void orderBy(String columnName) {
		//TODO
	}
	
	public void gropBy(String columnName) {
		//TODO
	}
	
	
	class LoadedColumn {
		public long[] valuesI;
		public double[] valuesF;
		public long[] valuesTS;
		public String[] valuesV;
		public final VarType columnType;
		
		
		public LoadedColumn(String filePath, VarType columnType, AggFuncs aggFunc) {
			this.columnType = columnType;
			//TODO load column and support aggFunc
		}
		
		private DBVar getItem(int i) {
			DBVar res = new DBVar();
			res.varType = columnType;
			switch (columnType) {
			case INT:
				res.i = valuesI[i];
				return res;
			case FLOAT:
				res.f = valuesF[i];
				return res;
			case TIMESTAMP:
				res.ts = valuesTS[i];
				return res;
			case VARCHAR:
				res.s = valuesV[i];
				return res;
			default://unreachable
				return null;
			}
		}
		
		public boolean[] getWhereIndexes(Where3 where) {
			boolean[] res = new boolean[rowsCount];
			for (int i = 0; i < res.length; i++) {
				res[i] = where.filter(getItem(i));
			}
			return res;
		}
		
		public void applayWhereIndexes(boolean[] indexes, int newRowsCount) {
			switch (columnType) {
			case INT:
				long[] newValuesI = new long[newRowsCount];
				int newIndI = 0;
				for (int oldInd = 0; oldInd < indexes.length; oldInd++)
					if (indexes[oldInd])
						newValuesI[newIndI++] = valuesI[oldInd];
				valuesI = newValuesI;
				break;
			case FLOAT:
				double[] newValuesF = new double[newRowsCount];
				int newIndF = 0;
				for (int oldInd = 0; oldInd < indexes.length; oldInd++)
					if (indexes[oldInd])
						newValuesF[newIndF++] = valuesF[oldInd];
				valuesF = newValuesF;
				break;
			case TIMESTAMP:
				long[] newValuesTS = new long[newRowsCount];
				int newIndTS = 0;
				for (int oldInd = 0; oldInd < indexes.length; oldInd++)
					if (indexes[oldInd])
						newValuesTS[newIndTS++] = valuesTS[oldInd];
				valuesTS = newValuesTS;
				break;
			case VARCHAR:
				String[] newValuesV = new String[newRowsCount];
				int newIndV = 0;
				for (int oldInd = 0; oldInd < indexes.length; oldInd++)
					if (indexes[oldInd])
						newValuesV[newIndV++] = valuesV[oldInd];
				valuesV = newValuesV;
				break;
			}
		}
	}
}
