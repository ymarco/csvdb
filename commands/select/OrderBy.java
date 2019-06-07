package commands.select;

public class OrderBy {
	public enum SortType {ASC,DESC};
	
	public final String outputFieldName;
	public final SortType sortType;
	
	public OrderBy(String outputFieldName, SortType sortType) {
		this.outputFieldName = outputFieldName;
		this.sortType = sortType;
	}
}
