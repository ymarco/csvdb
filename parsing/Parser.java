package parsing;

import java.util.ArrayList;

import commands.Command;
import commands.Create;
import commands.Drop;
import commands.Load;
import schema.Column;
import schema.VarType;

public class Parser {
	private final Tokenizer tkzr;
	private Token curr_token = null;

	public Parser(String cmd) {
		tkzr = new Tokenizer(cmd);
	}

	public Command parse() {
		nextToken();
		if (curr_token.type == TokenType.EOF)
			return null;
		if (curr_token.type != TokenType.KEYWORD)
			throwErr("first token wasnt a keyword");

		switch (curr_token.val) {
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
		curr_token = tkzr.nextToken();
	}

	private void expectNextToken(TokenType t, String v) {
		nextToken();
		if (curr_token.type != t || !curr_token.val.equals(v)) {
			String errmsg = "unexpected token: was expecting '" + t + "'";
			throwErr(errmsg);
		}
	}

	private void expectNextToken(TokenType tt) {
		nextToken();
		if (curr_token.type != tt) {
			String errmsg = "unexpected token: was expecting token of type '" + tt + "'";
			throwErr(errmsg);
		}
	}

	//
	private Command parseCreate() {
		String name = "";
		boolean enable_ifnexists = false;
		ArrayList<Column> args = new ArrayList<Column>();
		expectNextToken(TokenType.KEYWORD, "table");
		// check for [IF NOT EXISTS]
		nextToken();
		if (curr_token.equals(new Token(TokenType.KEYWORD, "if"))) {
			expectNextToken(TokenType.KEYWORD, "not");
			expectNextToken(TokenType.KEYWORD, "exists");
			nextToken();
			enable_ifnexists = true;
		}
		// check for table_name
		if (curr_token.type == TokenType.IDENTIFIER)
			name = curr_token.val;
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
			String arg_name = curr_token.val;
			expectNextToken(TokenType.KEYWORD); // argument type
			if (!Token.keywords.contains(curr_token.val))
				throwErr("parse error: insecondid database type");
			VarType arg_type = VarType.toVarType(curr_token.val);
			args.add(new Column(arg_type, arg_name));
			expectNextToken(TokenType.OPERATOR);
			if (curr_token.val.equals(",")) // more arguments
				continue;
			if (curr_token.val.equals(")")) { // end of arguments
				nextToken();
				break;
			}
			throwErr("unxpected token: expecting ',' or ')'");
		}
		/* finished reading arguments */
		expectNextToken(TokenType.EOF);
		return new Create(name, enable_ifnexists, (Column[]) args.toArray(new Column[0]));

	}

	private Command parseLoad() {
		String src;
		String dst;
		int ignore_lines = 0;
		expectNextToken(TokenType.KEYWORD, "data");
		expectNextToken(TokenType.KEYWORD, "infile");
		expectNextToken(TokenType.LIT_STR);
		src = curr_token.val;
		expectNextToken(TokenType.KEYWORD, "into");
		expectNextToken(TokenType.KEYWORD, "table");
		expectNextToken(TokenType.IDENTIFIER);
		dst = curr_token.val;
		nextToken();
		if (curr_token.type != TokenType.EOF) {
			expectNextToken(TokenType.KEYWORD, "ignore");
			expectNextToken(TokenType.LIT_NUM);
			ignore_lines = Integer.parseInt(curr_token.val);
		} else if (curr_token.type != TokenType.EOF) {
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
		if(curr_token.equals(new Token(TokenType.KEYWORD, "if"))) {
			expectNextToken(TokenType.KEYWORD, "exists");
			enable_ifexists = true;
			nextToken();
		}
		//check for table_name
		if(curr_token.type == TokenType.IDENTIFIER){
			name = curr_token.val;
		} else
			throwErr("KEYWORD _table_name not found");

		expectNextToken(TokenType.EOF);
		Drop res = new Drop(name, enable_ifexists);
		return res;
	}

	private Command parseSelect() {
		return null;
	}

}
