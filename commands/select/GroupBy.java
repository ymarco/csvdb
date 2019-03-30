package commands.select;

public class GroupBy {
	public String[] fieldsName;
	public Where having;

	public GroupBy(String[] fieldsName, Where having) {
		this.fieldsName = fieldsName;
		this.having = having;
	}
}
