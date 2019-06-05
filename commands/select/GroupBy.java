package commands.select;

public class GroupBy {
	public String[] fieldsName;
	public Where2 having;

	public GroupBy(String[] fieldsName, Where2 having) {
		this.fieldsName = fieldsName;
		this.having = having;
	}
}
