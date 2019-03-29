package commands.select;

public class GroupBy {
	public String[] fieldsName;
	public Condition having;
	
	public GroupBy(String[] fieldsName, Condition having) {
		this.fieldsName = fieldsName;
		this.having = having;
	}
}
