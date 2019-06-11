package commands.select;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import commands.Select3.Expression;
import commands.Select3.Expression.AggFuncs;
import commands.select.OrderBy.SortType;
import exceptions.CsvdbException;
import schema.Column2;
import schema.DBVar;
import schema.Schema;
import utils.Tuple;

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
			DBVar.Type colType = oldSchema.getColumnType(colInd);
			columns[i] = new LoadedColumn(filePath, colType, expression.aggFunc);
			
			schemaColumns[i] = new Column2(colType, expression.asName, filePath);
		}
		Schema.AddSchema(tableName, schemaColumns);
		this.schema = Schema.GetSchema(tableName);
	}
	
	private LoadedColumn getColumnByName(String columnName) {
		return columns[schema.getColumnIndex(columnName)];
	}
	
	public void where(Where3 where) {
		boolean[] whereIndexes = getColumnByName(where.fieldName).getWhereIndexes(where);
		int newRowsCount = 0;
		for (boolean index : whereIndexes)
			if (index)
				newRowsCount++;
		for (LoadedColumn column : columns)
			column.applayWhereIndexes(whereIndexes, newRowsCount);
		rowsCount = newRowsCount;
	}
	
	public void orderBy(OrderBy orderBy) {
		int[] orderByIndexes = getColumnByName(orderBy.outputFieldName).getOrderByIndexes(orderBy.sortType);
		for (LoadedColumn column : columns)
			column.applayOrderByIndexes(orderByIndexes);
	}
	
	public void gropBy(String columnName) {
		//TODO
	}
	
	
	
	
	
	
	
	
	
	
	
	class LoadedColumn {
		public long[] valuesI;
		public double[] valuesF;
		public long[] valuesTS;
		public String[] valuesV;
		public final DBVar.Type columnType;
		
		
		public LoadedColumn(String filePath, DBVar.Type columnType, AggFuncs aggFunc) {
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
			case TS:
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
			case TS:
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
		
		public int[] getOrderByIndexes(SortType sortType) {
			Comparator<DBVar> comparator = columnType.getComparator();
			List<Tuple<DBVar,Integer>> colDBvar = new ArrayList<Tuple<DBVar,Integer>>(rowsCount);
			for (int i = 0; i < rowsCount; i++)
				colDBvar.add(new Tuple<DBVar, Integer>(getItem(i), i));
			if (sortType == SortType.ASC)
				colDBvar.sort((x,y)->comparator.compare(x.f,y.f));
			else
				colDBvar.sort((x,y)-> -comparator.compare(x.f,y.f));
			
			int[] res = new int[colDBvar.size()];
			for (int i = 0; i < res.length; i++)
				res[i] = colDBvar.get(i).s;
			return res;
		}
		
		public void applayOrderByIndexes(int[] indexes) {
			switch (columnType) {
			case INT:
				long[] newValuesI = new long[rowsCount];
				for (int i = 0; i < newValuesI.length; i++)
					newValuesI[i] = valuesI[i];
				valuesI = newValuesI;
				break;
			case FLOAT:
				double[] newValuesF = new double[rowsCount];
				for (int i = 0; i < newValuesF.length; i++)
					newValuesF[i] = valuesF[i];
				valuesF = newValuesF;
				break;
			case TS:
				long[] newValuesTS = new long[rowsCount];
				for (int i = 0; i < newValuesTS.length; i++)
					newValuesTS[i] = valuesTS[i];
				valuesTS = newValuesTS;
				break;
			case VARCHAR:
				String[] newValuesV = new String[rowsCount];
				for (int i = 0; i < newValuesV.length; i++)
					newValuesV[i] = valuesV[i];
				valuesV = newValuesV;
				break;
			}
		}
	}
}
