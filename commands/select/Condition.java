package commands.select;

public class Condition {
	public String fieldName;
	public Operator op;
	public Object constant;
	
	public Condition(String fieldName, Operator op, Object constant) {
		this.fieldName = fieldName;
		this.op = op;
		this.constant = constant;
	}
}
