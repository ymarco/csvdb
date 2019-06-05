package commands;

import java.io.IOException;

import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.Where2;


public class Select2 implements Command {
	private final static String osName = System.getProperty("os.name").toLowerCase();
	private final static Runtime rt = Runtime.getRuntime();
	private static String cppfolderName;
	private static String cppExtName;
	
	static {
		if (osName.startsWith("windows")) {
			cppfolderName = "win";
			cppExtName = ".exe";
		}else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
			cppfolderName = "linux";
			cppExtName = "";
		} else 
			throw new Error("unknow os.name: " + osName);
	}
	
	
	
	private String intoFile;
	private String fromTableName;
	private Expression[] expressions;
	private Where2 where; //don't work now
	private GroupBy groupBy; //don't work now
	private OrderBy orderBy; //don't work now

	public Select2(String intoFile, String fromTableName, Expression[] expressions, Where2 where, GroupBy groupBy, OrderBy orderBy) {
		this.fromTableName = fromTableName;
		this.expressions = expressions;
		this.where = where;
		this.groupBy = groupBy;
		this.orderBy = orderBy;
	}

	public void run() {
		
	}
	
	public void runCpp(Object ... input) {
		String[] inputString = new String[input.length];
		for (int i = 0; i < inputString.length; i++)
			inputString[i] = input.toString();
		runCpp(inputString);
	}
	
	/**
	 * @param input [cppFileName, arg1, arg2, ...]
	 */
	public void runCpp(String ... input) {
	    try {
	        rt.exec(input);
	    } catch (IOException e) {
	        throw new RuntimeException(e);
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