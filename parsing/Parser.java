package parsing;

import java.util.ArrayList;
import java.util.List;

import commands.Command;
import commands.Create;
import commands.CreateAsSelect;
import commands.Drop;
import commands.Load;
import commands.Select;
import commands.Select.Expression;
import commands.Select.Expression.AggFuncs;
import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.Where;
import parsing.Token.Type;
import schema.Column2;
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
		ArrayList<Column2> args = new ArrayList<Column2>();
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
			args.add(new Column2(argType, argName, ""));
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
		return new Create(name, enable_ifnexists, args.toArray(new Column2[0]));

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
		Expression[] expressions = null;
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
		if (currToken.equals(new Token(Token.Type.KEYWORD, "where")))
			where = parseCondition(schema);
		//group by
		if (currToken.equals(new Token(Token.Type.KEYWORD, "group"))) {
			expectNextToken(Token.Type.KEYWORD, "by");
			List<String> fields = new ArrayList<>();
			do {
				expectNextToken(Token.Type.IDENTIFIER);
				fields.add(currToken.val);
				nextToken();
			}
			while (currToken.equals(new Token(Token.Type.OPERATOR, ",")));

			//having
			Where having = null;
			if (currToken.equals(new Token(Token.Type.KEYWORD, "having")))
				having = parseCondition(schema);

			groupBy = new GroupBy(fields.toArray(new String[0]), having);
		}
		//order by
		if (currToken.equals(new Token(Token.Type.KEYWORD, "order"))) {
			expectNextToken(Token.Type.KEYWORD, "by");
			expectNextToken(Token.Type.IDENTIFIER);
			String outputField = currToken.val;
			nextToken();

			boolean isDesc = false;
			if (currToken.type == Token.Type.KEYWORD) {
				if (currToken.val.equals("asc")) {
					isDesc = false;
				} else if (currToken.val.equals("desc"))
					isDesc = true;
				else
					throwErr("sort type can only be ASC or DESC");
			}
			orderBy = new OrderBy(schema.getTableName(), schema.getColumnIndex(outputField), isDesc);
			nextToken();
		}
		//eof
		expectNextToken(Token.Type.EOF);
		//return
		return new Select(intoFile, srcTableName, expressions,
				where, groupBy, orderBy, mode);
	}

	private Command parseCreateAsSelect(String tableName) {
		String fromTableName = "";
		Expression[] expressions = null;
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

	private Expression[] parseAllSelectExpression() {
		Expression[] expressions = null;
		nextToken();
		if (!currToken.equals(new Token(Token.Type.OPERATOR, "*"))) {
			List<Expression> expressionsList = new ArrayList<>();
			expressionsList.add(parseSelectExpression());
			while (currToken.equals(new Token(Token.Type.OPERATOR, ","))) {
				nextToken();
				expressionsList.add(parseSelectExpression());
				expressions = expressionsList.toArray(new Expression[expressionsList.size()]);
			}
		}
		return expressions;
	}

	private Expression parseSelectExpression() {
		AggFuncs aggFunc = AggFuncs.NOTHING;
		if (currToken.type == Token.Type.KEYWORD) {
			switch (currToken.val) {
			case "min":
				aggFunc = AggFuncs.MIN;
				break;
			case "max":
				aggFunc = AggFuncs.MAX;
				break;
			case "avg":
				aggFunc = AggFuncs.AVG;
				break;
			case "sum":
				aggFunc = AggFuncs.SUM;
				break;
			case "count":
				aggFunc = AggFuncs.COUNT;
				break;
			default:
				throwErr("agg func need to be: MIN or MAX or AVG or SUM or COUNT");
			}
			expectNextToken(Token.Type.OPERATOR, "(");
		}
		expectThisToken(Token.Type.IDENTIFIER);
		String fieldName = currToken.val;
		nextToken();
		if (aggFunc != AggFuncs.NOTHING) {
			expectThisToken(Token.Type.OPERATOR, ")");
			nextToken();
		}
		if (currToken.equals(new Token(Token.Type.KEYWORD, "as"))) {
			expectNextToken(Token.Type.IDENTIFIER);
			nextToken();
			return new Expression(fieldName, currToken.val);
		}
		return new Expression(fieldName);
	}

	private Where parseCondition(Schema schema) {
		/*COMMAND _field_name_ _operator_ _constant_
		 *COMMAND _field_name is [not] null */
		String operator = "";
		String constant = null; // in an 'is [not] null' case constant stays null; in any other case it is not.
		expectNextToken(Token.Type.IDENTIFIER);
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
			if (currToken.equals(new Token(Token.Type.IDENTIFIER, "null"))) throwErr("is [not] can only accept null");
			operator += "null";
			constant = null;
		} else if (currToken.type == Token.Type.OPERATOR) {
			operator = currToken.val;
			nextToken();
			constant = currToken.val;
		} else throwErr("invalid condition: " + currToken.val);
		return new Where(schema, schema.getColumnIndex(fieldName), operator, constant);
	}
}
