package commands.select;

public class GroupBy {
	public String[] fieldsName;
	public Where3 having;

	public GroupBy(String[] fieldsName, Where3 having) {
		this.fieldsName = fieldsName;
		this.having = having;
	}
}
