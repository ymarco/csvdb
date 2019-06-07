package parsing;

import java.util.ArrayList;
import java.util.List;

import commandLine.Main;
import commands.Command;
import commands.Create;
import commands.Drop;
import commands.Load;
import commands.Select2;
import commands.Select2.Expression;
import commands.Select2.Expression.AggFuncs;
import commands.select.GroupBy;
import commands.select.OrderBy;
import commands.select.Where2;
import schema.Column2;
import schema.Schema;
import schema.VarType;

public class Parser {
	private final Tokenizer tkzr;
	private Token currToken = null;

	public Parser(String cmd) {
		tkzr = new Tokenizer(cmd);
	}

	public Command parse() {
		nextToken();
		if (currToken.type == TokenType.EOF)
			return null;
		if (currToken.type != TokenType.KEYWORD)
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
			return null;
		}

	}

	private void throwErr(String msg) {
		tkzr.throwErr(msg);
	}

	private void nextToken() {
		currToken = tkzr.nextToken();
	}

	private void expectNextToken(TokenType t, String v) {
		nextToken();
		expectThisToken(t, v);
	}

	private void expectNextToken(TokenType tt) {
		nextToken();
		expectThisToken(tt);
	}
	
	private void expectThisToken(TokenType t, String v) {
		if (currToken.type != t || !currToken.val.equals(v)) {
			String errmsg = "unexpected token: was expecting '" + t + "'";
			throwErr(errmsg);
		}
	}

	private void expectThisToken(TokenType tt) {
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
		expectNextToken(TokenType.KEYWORD, "table");
		// check for [IF NOT EXISTS]
		nextToken();
		if (currToken.equals(new Token(TokenType.KEYWORD, "if"))) {
			expectNextToken(TokenType.KEYWORD, "not");
			expectNextToken(TokenType.KEYWORD, "exists");
			nextToken();
			enable_ifnexists = true;
		}
		// check for table_name
		if (currToken.type == TokenType.IDENTIFIER)
			name = currToken.val;
		else
			throwErr("KEYWORD _table_name not found");
		// check for "(" operator
		expectNextToken(TokenType.OPERATOR, "(");

		/*
		 * now reading arguments, consisting of IDENTIFIER (name), KEYWORD (type), //
		 * OPERATOR
		 */
		while (true) {
			expectNextToken(TokenType.IDENTIFIER); // argument name
			String argName = currToken.val;
			expectNextToken(TokenType.KEYWORD); // argument type
			if (!Token.keywords.contains(currToken.val))
				throwErr("parse error: insecondid database type");
			VarType argType = VarType.toVarType(currToken.val);
			args.add(new Column2(argType, argName, Main.rootdir + "//" + name + "//" + argName + ".col"));
			expectNextToken(TokenType.OPERATOR);
			if (currToken.val.equals(",")) // more arguments
				continue;
			if (currToken.val.equals(")")) { // end of arguments
				nextToken();
				break;
			}
			throwErr("unxpected token: expecting ',' or ')'");
		}
		/* finished reading arguments */
		expectNextToken(TokenType.EOF);
		return new Create(name, enable_ifnexists, (Column2[]) args.toArray(new Column2[0]));

	}

	private Command parseLoad() {
		String src;
		String dst;
		int ignore_lines = 0;
		expectNextToken(TokenType.KEYWORD, "data");
		expectNextToken(TokenType.KEYWORD, "infile");
		expectNextToken(TokenType.LIT_STR);
		src = currToken.val;
		expectNextToken(TokenType.KEYWORD, "into");
		expectNextToken(TokenType.KEYWORD, "table");
		expectNextToken(TokenType.IDENTIFIER);
		dst = currToken.val;
		nextToken();
		if (currToken.type != TokenType.EOF) {
			expectNextToken(TokenType.KEYWORD, "ignore");
			expectNextToken(TokenType.LIT_NUM);
			ignore_lines = Integer.parseInt(currToken.val);
		} else if (currToken.type != TokenType.EOF) {
			throwErr("unexpected token");
		}
		return new Load(src, dst, ignore_lines);
	}

	private Command parseDrop() {
		boolean enable_ifexists = false;
		String name = "";
		// check for table kw
		expectNextToken(TokenType.KEYWORD, "table");
		// check for [IF EXISTS]
		nextToken();
		if(currToken.equals(new Token(TokenType.KEYWORD, "if"))) {
			expectNextToken(TokenType.KEYWORD, "exists");
			enable_ifexists = true;
			nextToken();
		}
		//check for table_name
		if(currToken.type == TokenType.IDENTIFIER){
			name = currToken.val;
		} else
			throwErr("KEYWORD _table_name not found");

		expectNextToken(TokenType.EOF);
		Drop res = new Drop(name, enable_ifexists);
		return res;
	}

	private Command parseSelect() {
		String intoFile = null;
		String fromTableName;
		Expression[] expressions = null;
		Where2 where = null;
		GroupBy groupBy = null;
		OrderBy orderBy = null;
		
		
		//expression
		nextToken();
		if (!currToken.equals(new Token(TokenType.OPERATOR, "*"))) {
			List<Expression> expressionsList = new ArrayList<Expression>();
			expressionsList.add(parseSelectExpression());
			while (currToken.equals(new Token(TokenType.OPERATOR, ","))) {
				nextToken();
				expressionsList.add(parseSelectExpression());
			}
		}
		//into outfile
		if (currToken.equals(new Token(TokenType.KEYWORD, "into"))) {
			expectNextToken(TokenType.KEYWORD, "outfile");
			expectNextToken(TokenType.LIT_STR);
			intoFile = currToken.val;
			nextToken();
		}
		//from
		expectThisToken(TokenType.KEYWORD, "from");
		expectNextToken(TokenType.IDENTIFIER);
		fromTableName = currToken.val;
		if (!Schema.HaveSchema(fromTableName))
			throwErr("unexisting table");
		Schema schema = Schema.GetSchema(fromTableName);
		//where
		nextToken();
		if (currToken.equals(new Token(TokenType.KEYWORD, "where")))
			where = parseCondition(schema);
		//group by
		if (currToken.equals(new Token(TokenType.KEYWORD, "group"))) {
			expectNextToken(TokenType.KEYWORD, "by");
			List<String> fields = new ArrayList<String>();
			do
			{
				expectNextToken(TokenType.IDENTIFIER);
				fields.add(currToken.val);
				nextToken();
			}
			while (currToken.equals(new Token(TokenType.OPERATOR, ",")));
			
			//having
			Where2 having = null;
			if (currToken.equals(new Token(TokenType.KEYWORD, "having")))
				having = parseCondition(schema);
			
			groupBy = new GroupBy(fields.toArray(new String[0]), having);
		}
		//order by
		if (currToken.equals(new Token(TokenType.KEYWORD, "order"))) {
			expectNextToken(TokenType.KEYWORD, "by");
			expectNextToken(TokenType.IDENTIFIER);
			String outputField = currToken.val;
			nextToken();
			
			OrderBy.SortType sortType = OrderBy.SortType.ASC;
			if (currToken.type == TokenType.KEYWORD) {
				if (currToken.val.equals("asc"));
				else if (currToken.val.equals("desc"))
					sortType = OrderBy.SortType.DESC;
				else
					throwErr("sort type need to be ASC or DESC");
			}
			orderBy = new OrderBy(outputField, sortType);
			nextToken();
		}
		//eof
		expectNextToken(TokenType.EOF);
		//return
		return new Select2(intoFile, fromTableName, expressions, where, groupBy, orderBy);
	}
	
	private Expression parseSelectExpression() {
		AggFuncs aggFunc = AggFuncs.NOTHING;
		if (currToken.type == TokenType.KEYWORD) {
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
			expectNextToken(TokenType.OPERATOR, "(");
		}
		expectThisToken(TokenType.IDENTIFIER);
		String fieldName = currToken.val;
		nextToken();
		if (aggFunc != AggFuncs.NOTHING) {
			expectThisToken(TokenType.OPERATOR, ")");
			nextToken();
		}
		if (currToken.equals(new Token(TokenType.KEYWORD, "as"))) {
			expectNextToken(TokenType.IDENTIFIER);
			nextToken();
			return new Expression(fieldName, currToken.val);
		}
		return new Expression(fieldName);
	}
	
	private Where2 parseCondition(Schema schema) {
		expectNextToken(TokenType.IDENTIFIER);
		String fieldName = currToken.val;
		expectNextToken(TokenType.OPERATOR);
		String operator = currToken.val;
		nextToken();
		if (currToken.type != TokenType.LIT_NUM || currToken.type != TokenType.LIT_STR)
			throwErr("unexpected token");
		String constant = currToken.val;
		nextToken();
		return new Where2(schema, fieldName, operator, constant);
	}
}
