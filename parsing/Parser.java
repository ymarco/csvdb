package parsing;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import commands.Command;
import commands.Create;
import commands.CreateAsSelect;
import commands.Drop;
import commands.Load;
import commands.Select;
import commands.select.*;
import parsing.Token.Type;
import schema.Column;
import schema.DBVar;
import schema.Schema;

public class Parser {
	private final Tokenizer tkzr;
	private Token currToken = null;

	public Parser(String cmd) {
		tkzr = new Tokenizer(cmd);
	}

	public Command parse() {
		nextToken();
		if (currToken.type == Token.Type.EOF)
			return Command.emptyCommand;
		if (currToken.type != Token.Type.KEYWORD)
			throwErr("first token wasnt a keyword");

		switch (currToken.val) {
			case "create":
				return parseCreate();
			case "drop":
				return parseDrop();
			case "load":
				return parseLoad();
			case "select":
				return parseSelect();
			/* unreachable */
			default:
				return null; //TODO add special Exceptions
		}

	}

	private void throwErr(String msg) {
		tkzr.throwErr(msg);
	}

	private void nextToken() {
		currToken = tkzr.nextToken();
	}

	private void expectNextToken(Token.Type t, String v) {
		nextToken();
		expectThisToken(t, v);
	}

	private void expectNextToken(Token.Type tt) {
		nextToken();
		expectThisToken(tt);
	}

	private void expectThisToken(Token.Type t, String v) {
		if (currToken.type != t || !currToken.val.equals(v)) {
			String errmsg = "unexpected token: was expecting '" + t + "'";
			throwErr(errmsg);
		}
	}

	private void expectThisToken(Token.Type tt) {
		if (currToken.type != tt) {
			String errmsg = "unexpected token: was expecting token of type '" + tt + "'";
			throwErr(errmsg);
		}
	}

	//
	private Command parseCreate() {
		String name = "";
		boolean enable_ifnexists = false;
		ArrayList<Column> args = new ArrayList<Column>();
		expectNextToken(Token.Type.KEYWORD, "table");

		// check for [IF NOT EXISTS]
		nextToken();
		if (currToken.equals(new Token(Token.Type.KEYWORD, "if"))) {
			expectNextToken(Token.Type.KEYWORD, "not");
			expectNextToken(Token.Type.KEYWORD, "exists");
			nextToken();
			enable_ifnexists = true;
		}
		// check for table_name
		if (currToken.type == Token.Type.IDENTIFIER)
			name = currToken.val;
		else
			throwErr("KEYWORD _table_name not found");

		nextToken();
		// check Create As Select
		if (currToken.equals(new Token(Token.Type.KEYWORD, "as"))) {
			return parseCreateAsSelect(name);
		}

		// check for "(" operator
		expectThisToken(Token.Type.OPERATOR, "(");

		/*
		 * now reading arguments, consisting of IDENTIFIER (name), KEYWORD (type), //
		 * OPERATOR
		 */
		while (true) {
			expectNextToken(Token.Type.IDENTIFIER); // argument name
			String argName = currToken.val;
			expectNextToken(Token.Type.KEYWORD); // argument type
			if (!Token.keywords.contains(currToken.val))
				throwErr("parse error: invalid database type");
			DBVar.Type argType = DBVar.Type.toVarType(currToken.val);
			args.add(new Column(argType, argName));
			expectNextToken(Token.Type.OPERATOR);
			if (currToken.val.equals(",")) // more arguments
				continue;
			if (currToken.val.equals(")")) { // end of arguments
				nextToken();
				break;
			}
			throwErr("unxpected token: expecting ',' or ')'");
		}
		/* finished reading arguments */
		expectNextToken(Token.Type.EOF);
		return new Create(name, enable_ifnexists, args.toArray(new Column[0]));

	}

	private Command parseLoad() {
		String src;
		String dst;
		int ignore_lines = 0;
		expectNextToken(Token.Type.KEYWORD, "data");
		expectNextToken(Token.Type.KEYWORD, "infile");
		expectNextToken(Token.Type.LIT_STR);
		src = currToken.val;
		expectNextToken(Token.Type.KEYWORD, "into");
		expectNextToken(Token.Type.KEYWORD, "table");
		expectNextToken(Token.Type.IDENTIFIER);
		dst = currToken.val;
		nextToken();
		if (currToken.type != Token.Type.EOF) {
			expectNextToken(Token.Type.KEYWORD, "ignore");
			expectNextToken(Token.Type.LIT_NUM);
			ignore_lines = Integer.parseInt(currToken.val);
		}
		if (currToken.type != Token.Type.EOF) {
			throwErr("unexpected token");
		}
		return new Load(src, dst, ignore_lines);
	}

	private Command parseDrop() {
		boolean enable_ifexists = false;
		String name = "";
		// check for table kw
		expectNextToken(Token.Type.KEYWORD, "table");
		// check for [IF EXISTS]
		nextToken();
		if (currToken.equals(new Token(Token.Type.KEYWORD, "if"))) {
			expectNextToken(Token.Type.KEYWORD, "exists");
			enable_ifexists = true;
			nextToken();
		}
		//check for table_name
		if (currToken.type == Token.Type.IDENTIFIER) {
			name = currToken.val;
		} else
			throwErr("KEYWORD _table_name not found");

		expectNextToken(Token.Type.EOF);
		return new Drop(name, enable_ifexists);
	}

	private Command parseSelect() {
		String intoFile = null;
		String srcTableName;
		SelectExpression[] expressions = null;
		Where where = null;
		GroupBy groupBy = null;
		OrderBy orderBy = null;
		/* mode can only be PRINT_TO_SCREEN or EXPORT_TO_CSV;
		 * CREATE_NEW_TABLE is implemented in create as select.*/
		Select.Mode mode = Select.Mode.PRINT_TO_SCREEN;


		//expression
		expressions = parseAllSelectExpression();
		//into outfile
		if (currToken.equals(new Token(Token.Type.KEYWORD, "into"))) {
			mode = Select.Mode.EXPORT_TO_CSV;
			expectNextToken(Token.Type.KEYWORD, "outfile");
			expectNextToken(Token.Type.LIT_STR);
			intoFile = currToken.val;
			nextToken();
		}
		//from
		expectThisToken(Token.Type.KEYWORD, "from");
		expectNextToken(Token.Type.IDENTIFIER);
		srcTableName = currToken.val;
		Schema schema = Schema.GetSchema(srcTableName);
		//where
		nextToken();
		if (currToken.equals(new Token(Token.Type.KEYWORD, "where"))) {
			nextToken();
			where = parseCondition(schema);
		}
		List<String> fieldsToGroupBy;
		//group by
		boolean groupingBy = false;
		if (currToken.equals(new Token(Type.KEYWORD, "group"))) {
			//groupBy (declared)
			expectNextToken(Type.KEYWORD, "by");
			groupingBy = true;
			fieldsToGroupBy = parseIdentifierList();
		} else {
			//groupBy (not declared)
			fieldsToGroupBy = new ArrayList<>();
			if (Arrays.stream(expressions).map(e -> e.agg).filter(Aggregator::isEmpty).count() == 0) { // all expressions have aggregators
				groupingBy = true;
			}

		}
		if (groupingBy) {
			//having
			Where having = null;
			if (currToken.equals(new Token(Type.KEYWORD, "having"))) {
				nextToken();
				having = parseHavingCondition(schema, expressions);
			}
			int[] colNumsToGroupBy = fieldsToGroupBy.stream().mapToInt(schema::getColumnIndex).toArray();

			groupBy = new GroupBy(srcTableName, colNumsToGroupBy, expressions, having);
		}


		//order by
		if (currToken.equals(new Token(Token.Type.KEYWORD, "order"))) {
			//TODO parse order by to more than one column
			expectNextToken(Token.Type.KEYWORD, "by");

			List<String> orderByColNames = new ArrayList<>();
			List<Boolean> isDesc = new ArrayList<>();
			do {
				expectNextToken(Type.IDENTIFIER);
				orderByColNames.add(currToken.val);
				nextToken();
				if (currToken.type == Type.KEYWORD) { // asc or desc
					if (currToken.val.equals("asc")) {
						isDesc.add(false);
						nextToken();
					} else if (currToken.val.equals("desc")) {
						isDesc.add(true);
						nextToken();
					} else throwErr("sorting type must be asc or desc");
				} else {
					isDesc.add(false); // default is asc
				}
			} while (currToken.equals(new Token(Type.OPERATOR, ",")));

			int[] orderByColNums;
			if (groupingBy) {
				orderByColNums = getColNumsFromExpressions(expressions, orderByColNames);
			} else {
				orderByColNums = orderByColNames.stream().mapToInt(schema::getColumnIndex).toArray();
			}
			boolean[] isDescArray = new boolean[isDesc.size()];
			IntStream.range(0, isDesc.size()).forEach(i -> isDescArray[i] = isDesc.get(i));
			orderBy = new OrderBy(orderByColNums, isDescArray);
			nextToken();
		}

		//eof
		expectNextToken(Token.Type.EOF);
		//return
		return new

				Select(intoFile, srcTableName, expressions,
				where, groupBy, orderBy, mode);

	}

	private int[] getColNumsFromExpressions(SelectExpression[] expressions, List<String> orderByColNames) {
		int[] orderByColNums;
		orderByColNums = new int[orderByColNames.size()];
		for (int i = 0; i < orderByColNames.size(); i++) {
			//find expression with the same name as orderByColNames.get(i)
			boolean hasFound = false;
			for (int j = 0; j < expressions.length; j++) {
				if (orderByColNames.get(i).equals(expressions[j].asName)) {
					orderByColNums[i] = j;
					hasFound = true;
					break;
				}
			}
			if (!hasFound) throwErr("column " + orderByColNames.get(i) + " was not found");
		}
		return orderByColNums;
	}

	private List<String> parseIdentifierList() {
		List<String> res = new ArrayList<>();
		do {
			expectNextToken(Type.IDENTIFIER);
			res.add(currToken.val);
			nextToken();
		} while (currToken.equals(new Token(Type.OPERATOR, ",")));
		return res;
	}

	private Command parseCreateAsSelect(String tableName) {
		String fromTableName = "";
		SelectExpression[] expressions = null;
		Where where = null;

		expectNextToken(Type.KEYWORD, "select");
		expressions = parseAllSelectExpression();
		expectThisToken(Type.KEYWORD, "from");
		expectNextToken(Type.IDENTIFIER);
		fromTableName = currToken.val;
		Schema schema = Schema.GetSchema(fromTableName); //TODO if there is no schema named tableName
		nextToken();
		if (currToken.equals(new Token(Token.Type.KEYWORD, "where")))
			where = parseCondition(schema);
		expectThisToken(Type.EOF);
		return new CreateAsSelect(tableName, fromTableName, expressions, where);
	}

	private SelectExpression[] parseAllSelectExpression() {
		SelectExpression[] expressions = null;
		nextToken();
		if (currToken.equals(new Token(Type.OPERATOR, "*"))) {
			return null;
		}

		List<SelectExpression> expressionsList = new ArrayList<>();
		// first expression
		expressionsList.add(parseSelectExpression());
		// other expressions separated by ,
		while (currToken.equals(new Token(Token.Type.OPERATOR, ","))) {
			nextToken();
			expressionsList.add(parseSelectExpression());
		}
		expressions = expressionsList.toArray(new SelectExpression[expressionsList.size()]);
		return expressions;
	}

	private SelectExpression parseSelectExpression() {
		String fieldName;
		String asName;
		Aggregator agg = new Aggregator.EmptyAgg();
		boolean hasAgg = false;
		if (currToken.type == Token.Type.KEYWORD) {
			hasAgg = true;
			switch (currToken.val) {
				case "min":
					agg = new Aggregator.Min();
					break;
				case "max":
					agg = new Aggregator.Max();
					break;
				case "avg":
					agg = new Aggregator.Avg();
					break;
				case "sum":
					agg = new Aggregator.Sum();
					break;
				case "count":
					agg = new Aggregator.Count();
					break;
				default:
					throwErr("agg func can only be min,max,avg,sum,count");
			}
			expectNextToken(Token.Type.OPERATOR, "(");
			nextToken();
		}
		expectThisToken(Token.Type.IDENTIFIER);
		fieldName = currToken.val;
		nextToken();
		if (hasAgg) {
			expectThisToken(Token.Type.OPERATOR, ")");
			nextToken();
		}
		if (currToken.equals(new Token(Token.Type.KEYWORD, "as"))) {
			expectNextToken(Token.Type.IDENTIFIER);
			asName = currToken.val;
			nextToken();
		} else {
			asName = fieldName;
		}
		return new SelectExpression(fieldName, asName, agg);
	}

	private Where parseHavingCondition(Schema schema, SelectExpression[] expressions) {
		/*COMMAND _field_name_ _operator_ _constant_
		 *COMMAND _field_name is [not] null */
		String operator = "";
		String constant = null; // in an 'is [not] null' case constant stays null; in any other case it is not.
		expectThisToken(Token.Type.IDENTIFIER);
		String fieldName = currToken.val;
		nextToken();
		// is [not] null
		if (currToken.equals(new Token(Token.Type.IDENTIFIER, "is"))) {
			operator += "is ";
			nextToken();
			if (currToken.equals(new Token(Token.Type.IDENTIFIER, "not"))) {
				operator += "not ";
				nextToken();
			}
			expectThisToken(Type.KEYWORD, "null");
			operator += "null";
			constant = null;
		} else if (currToken.type == Token.Type.OPERATOR) {
			operator = currToken.val;
			nextToken();
			constant = currToken.val;
		} else throwErr("invalid condition: " + currToken.val);
		SelectExpression havingExpression = null;
		int colNum = 0;
		// find the column num where col.asName = fieldName
		for (int i = 0; i < expressions.length; i++) {
			if (expressions[i].asName.equals(fieldName)) {
				havingExpression = expressions[i];
				colNum = i;
			}
		}
		if (havingExpression == null) throwErr("having: didnt find the selected column");
		DBVar.Type type;
		switch (havingExpression.agg.getClass().getSimpleName()) {
			case "Min":
			case "Max":
				type = schema.getColumnType(havingExpression.fieldName);
				break;
			case "Count":
				type = DBVar.Type.INT;
				break;
			case "Sum":
			case "Avg":
				type = DBVar.Type.FLOAT;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + havingExpression.agg.getClass().getSimpleName());
		}
		nextToken();
		return new Where(type, colNum, operator, constant);
	}
	private Where parseCondition(Schema schema) {
		/*COMMAND _field_name_ _operator_ _constant_
		 *COMMAND _field_name is [not] null */
		String operator = "";
		String constant = null; // in an 'is [not] null' case constant stays null; in any other case it is not.
		expectThisToken(Token.Type.IDENTIFIER);
		String fieldName = currToken.val;
		nextToken();
		// is [not] null
		if (currToken.equals(new Token(Token.Type.IDENTIFIER, "is"))) {
			operator += "is ";
			nextToken();
			if (currToken.equals(new Token(Token.Type.IDENTIFIER, "not"))) {
				operator += "not ";
				nextToken();
			}
			expectThisToken(Type.KEYWORD, "null");
			operator += "null";
			constant = null;
		} else if (currToken.type == Token.Type.OPERATOR) {
			operator = currToken.val;
			nextToken();
			constant = currToken.val;
		} else throwErr("invalid condition: " + currToken.val);
		nextToken();
		return new Where(schema.getColumnType(fieldName), schema.getColumnIndex(fieldName), operator, constant);
	}
}
