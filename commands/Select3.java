package commands;

import java.io.IOException;

import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.Where2;
import commands.select.Where3;


public class Select3 implements Command {
	private String intoFile;
	private String fromTableName;
	private Expression[] expressions;
	private Where3 where; //don't work now
	private GroupBy groupBy; //don't work now
	private OrderBy orderBy; //don't work now

	public Select3(String intoFile, String fromTableName, Expression[] expressions, Where3 where, GroupBy groupBy, OrderBy orderBy) {
		this.fromTableName = fromTableName;
		this.expressions = expressions;
		this.where = where;
		this.groupBy = groupBy;
		this.orderBy = orderBy;
	}

	public void run() {
		
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