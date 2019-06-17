package commands.select;

import schema.DBVar;
import schema.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commands.Select.Expression;
import commands.Select.Expression.AggFuncs;

public class GroupBy implements Statement {
	public String[] fieldsToGroupBy;
	public Expression[] expressions;
	public Where having;
	public Schema schema;

	public GroupBy(String[] fieldsToGroupBy, Expression[] expressions, Where having) {
		this.fieldsToGroupBy = fieldsToGroupBy;
		this.expressions = expressions;
		this.having = having;
	}

	@Override
	public Stream<DBVar[]> apply(Stream<DBVar[]> s) {
		//make rowToVector, aggFunc
		Function<DBVar[], Vector<DBVar>> rowToGroupFields;
		Function<DBVar[], Vector<DBVar>> rowToAggFields;
		{
			List<Integer> groupFieldsIndexes = new ArrayList<>();
			List<Integer> aggFieldsIndexes = new ArrayList<>();
			for (int i = 0; i < expressions.length; i++)
				(expressions[i].aggFunc == AggFuncs.NOTHING ? groupFieldsIndexes : aggFieldsIndexes).add(i);
			rowToGroupFields = (x)->new Vector<>(groupFieldsIndexes.stream().map((y)->x[y]).collect(Collectors.toList()));
			rowToAggFields = (x)->new Vector<>(aggFieldsIndexes.stream().map((y)->x[y]).collect(Collectors.toList()));
		}
		//TODO translateExpressionsToLists
		Map<Vector<DBVar>, Vector<DBVar>> rows = new HashMap<>(); 
		s.forEach((x)->{
			Vector<DBVar> groupFields = rowToGroupFields.apply(x);
			Vector<DBVar> aggFields = rowToAggFields.apply(x);
			if (!rows.containsKey(groupFields))
				rows.put(groupFields, aggFields);
			else {
				Vector<DBVar> preAggFields = rows.get(groupFields);
				
			}
			//			Vector vector = new Vector
			//					if (rows.containsKey(x));
		});


		//TODO groping
		//		s = s.group

		//having
		if (having != null)
			s = having.apply(s);


		return s; 
	}
	
	
	class GroupByRow {
		public static List<Integer> 
	}
}
