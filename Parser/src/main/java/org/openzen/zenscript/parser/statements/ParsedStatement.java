package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.WhitespacePostComment;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

public abstract class ParsedStatement {
	public static ParsedFunctionBody parseLambdaBody(ZSTokenStream tokens, boolean inExpression) {
		CodePosition position = tokens.getPosition();
		ZSToken start = tokens.peek();
		if (tokens.optional(T_AOPEN) != null) {
			List<ParsedStatement> statements = new ArrayList<>();
			while (tokens.optional(T_ACLOSE) == null)
				statements.add(ParsedStatement.parse(tokens));
			
			return new ParsedStatementsFunctionBody(new ParsedStatementBlock(position, null, null, statements));
		} else {
			ParsedFunctionBody result = new ParsedLambdaFunctionBody(ParsedExpression.parse(tokens));
			if (!inExpression)
				tokens.required(T_SEMICOLON, "; expected");
			return result;
		}
	}
	
	public static ParsedFunctionBody parseFunctionBody(ZSTokenStream tokens) {
		if (tokens.optional(T_LAMBDA) != null)
			return parseLambdaBody(tokens, false);
		else if (tokens.optional(T_SEMICOLON) != null)
			return new ParsedEmptyFunctionBody(tokens.getPosition());
		else
			return new ParsedStatementsFunctionBody(parseBlock(tokens, true));
	}
	
	public static ParsedStatementBlock parseBlock(ZSTokenStream parser, boolean isFirst) {
		String ws = parser.getLastWhitespace();
		CodePosition position = parser.getPosition();
		parser.required(T_AOPEN, "{ expected");
		parser.skipWhitespaceNewline();

		ArrayList<ParsedStatement> statements = new ArrayList<>();
		ZSToken last;
		boolean firstContent = true;
		while ((last = parser.optional(T_ACLOSE)) == null) {
			statements.add(parse(parser, firstContent));
			firstContent = false;
		}

		WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
		WhitespacePostComment postComment = WhitespacePostComment.fromWhitespace(parser.getLastWhitespace());
		return new ParsedStatementBlock(position, whitespace, postComment, statements);
	}
	
	public static ParsedStatement parse(ZSTokenStream parser) {
		return parse(parser, false);
	}
	
	public static ParsedStatement parse(ZSTokenStream parser, boolean isFirst) {
		String ws = parser.getLastWhitespace();
		CodePosition position = parser.getPosition();
		ZSToken next = parser.peek();
		switch (next.getType()) {
			case T_AOPEN:
				return parseBlock(parser, isFirst);
			
			case K_RETURN: {
				parser.next();
				ParsedExpression expression = null;
				if (!parser.isNext(T_SEMICOLON)) {
					expression = ParsedExpression.parse(parser);
				}
				parser.required(T_SEMICOLON, "; expected");
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementReturn(position, whitespace, expression);
			}
			case K_VAR:
			case K_VAL: {
				ZSToken start = parser.next();
				String name = parser.required(T_IDENTIFIER, "identifier expected").content;

				IParsedType type = null;
				ParsedExpression initializer = null;
				if (parser.optional(K_AS) != null) {
					type = IParsedType.parse(parser);
				}
				if (parser.optional(T_ASSIGN) != null) {
					initializer = ParsedExpression.parse(parser);
				}
				parser.required(T_SEMICOLON, "; expected");
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementVar(position, whitespace, name, type, initializer, start.getType() == K_VAL);
			}
			case K_IF: {
				parser.next();
				ParsedExpression expression = ParsedExpression.parse(parser);
				parser.skipWhitespaceNewline();
				ParsedStatement onIf = parse(parser);
				ParsedStatement onElse = null;
				if (parser.optional(K_ELSE) != null) {
					parser.skipWhitespaceNewline();
					onElse = parse(parser);
				}
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementIf(position, whitespace, expression, onIf, onElse);
			}
			case K_FOR: {
				parser.next();
				String name = parser.required(T_IDENTIFIER, "identifier expected").content;
				List<String> names = new ArrayList<>();
				names.add(name);

				while (parser.optional(T_COMMA) != null) {
					names.add(parser.required(T_IDENTIFIER, "identifier expected").content);
				}

				parser.required(K_IN, "in expected");
				ParsedExpression source = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementForeach(
						position,
						whitespace,
						names.toArray(new String[names.size()]),
						source,
						content);
			}
			case K_DO: {
				ZSToken t = parser.next();
				String label = null;
				if (parser.optional(T_COLON) != null)
					label = parser.required(T_IDENTIFIER, "identifier expected").content;
				
				ParsedStatement content = parse(parser);
				parser.required(K_WHILE, "while expected");
				ParsedExpression condition = ParsedExpression.parse(parser);
				parser.required(T_SEMICOLON, "; expected");
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementDoWhile(position, whitespace, label, content, condition);
			}
			case K_WHILE: {
				ZSToken t = parser.next();
				String label = null;
				if (parser.optional(T_COLON) != null)
					label = parser.required(T_IDENTIFIER, "identifier expected").content;
				
				ParsedExpression condition = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementWhile(position, whitespace, label, condition, content);
			}
			case K_LOCK: {
				ZSToken t = parser.next();
				ParsedExpression object = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementLock(position, whitespace, object, content);
			}
			case K_THROW: {
				ZSToken t = parser.next();
				ParsedExpression value = ParsedExpression.parse(parser);
				parser.required(T_SEMICOLON, "; expected");
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementThrow(position, whitespace, value);
			}
			case K_TRY: {
				parser.pushMark();
				ZSToken t = parser.next();
				
				if (parser.peek().type == T_QUEST || parser.peek().type == T_NOT) {
					// Stop! This is a try! or try? expression...
					parser.reset();
					break;
				}
				
				parser.popMark();
				
				String name = null;
				ParsedExpression initializer = null;
				
				if (parser.isNext(ZSTokenType.T_IDENTIFIER)) {
					name = parser.next().content;
					parser.required(ZSTokenType.T_ASSIGN, "= expected");
					initializer = ParsedExpression.parse(parser);
				}
				
				ParsedStatement content = parse(parser);
				List<ParsedCatchClause> catchClauses = new ArrayList<>();
				while (parser.optional(K_CATCH) != null) {
					CodePosition catchPosition = parser.getPosition();
					
					String catchName = null;
					if (parser.isNext(ZSTokenType.T_IDENTIFIER))
						catchName = parser.next().content;
					
					IParsedType catchType = ParsedTypeBasic.ANY;
					if (parser.optional(K_AS) != null)
						catchType = IParsedType.parse(parser);
					
					ParsedStatement catchContent = ParsedStatement.parse(parser);
					catchClauses.add(new ParsedCatchClause(catchPosition, catchName, catchType, catchContent));
				}
				
				ParsedStatement finallyClause = null;
				if (parser.optional(K_FINALLY) != null)
					finallyClause = ParsedStatement.parse(parser);
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementTryCatch(position, whitespace, name, initializer, content, catchClauses, finallyClause);
			}
			case K_CONTINUE: {
				ZSToken t = parser.next();
				String name = null;
				if (parser.isNext(T_IDENTIFIER))
					name = parser.next().content;
				
				parser.required(T_SEMICOLON, "; expected");
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementContinue(position, whitespace, name);
			}
			case K_BREAK: {
				ZSToken t = parser.next();
				String name = null;
				if (parser.isNext(T_IDENTIFIER))
					name = parser.next().content;
				
				parser.required(T_SEMICOLON, "; expected");
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementBreak(position, whitespace, name);
			}
			case K_SWITCH: {
				ZSToken t = parser.next();
				String name = null;
				if (parser.optional(T_COLON) != null)
					name = parser.next().content;
				
				ParsedExpression value = ParsedExpression.parse(parser);
				ParsedSwitchCase currentCase = null;
				List<ParsedSwitchCase> cases = new ArrayList<>();
				
				parser.required(T_AOPEN, "{ expected");
				while (parser.optional(T_ACLOSE) == null) {
					if (parser.optional(K_CASE) != null) {
						currentCase = new ParsedSwitchCase(ParsedExpression.parse(parser));
						cases.add(currentCase);
						parser.required(T_COLON, ": expected");
					} else if (parser.optional(K_DEFAULT) != null) {
						currentCase = new ParsedSwitchCase(null);
						cases.add(currentCase);
						parser.required(T_COLON, ": expected");
					} else if (currentCase == null) {
						throw new CompileException(parser.getPosition(), CompileExceptionCode.STATEMENT_OUTSIDE_SWITCH_CASE, "Statement in switch outside case");
					} else {
						currentCase.statements.add(ParsedStatement.parse(parser));
					}
				}
				
				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementSwitch(position, whitespace, name, value, cases);
			}
		}
		
		ParsedExpression expression = ParsedExpression.parse(parser);
		parser.required(T_SEMICOLON, "; expected");
		WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
		
		ParsedStatementExpression result = new ParsedStatementExpression(position, whitespace, expression);
		return result;
	}
	
	public static List<Statement> compile(List<ParsedStatement> statements, StatementScope scope) {
		if (statements == null) // no body
			return null;
		
		List<Statement> result = new ArrayList<>();
		for (ParsedStatement statement : statements)
			result.add(statement.compile(scope));
		return result;
	}

	public final CodePosition position;
	public final WhitespaceInfo whitespace;

	public ParsedStatement(CodePosition position, WhitespaceInfo whitespace) {
		this.position = position;
		this.whitespace = whitespace;
	}

	public abstract Statement compile(StatementScope scope);
	
	protected Statement result(Statement statement) {
		statement.setTag(WhitespaceInfo.class, whitespace);
		return statement;
	}
}
