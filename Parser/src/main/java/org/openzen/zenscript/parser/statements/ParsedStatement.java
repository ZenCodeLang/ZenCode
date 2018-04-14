package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
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

public abstract class ParsedStatement {
	public static ParsedFunctionBody parseLambdaBody(ZSTokenStream tokens, boolean inExpression) {
		if (tokens.optional(T_AOPEN) != null) {
			List<ParsedStatement> statements = new ArrayList<>();
			while (tokens.optional(T_ACLOSE) == null)
				statements.add(ParsedStatement.parse(tokens));
			
			return new ParsedStatementsFunctionBody(statements);
		} else {
			ParsedFunctionBody result = new ParsedLambdaFunctionBody(ParsedExpression.parse(tokens));
			if (!inExpression)
				tokens.required(T_SEMICOLON, "; expected");
			return result;
		}
	}
	
	public static ParsedFunctionBody parseFunctionBody(ZSTokenStream tokens) {
		if (tokens.optional(T_LAMBDA) != null) {
			return parseLambdaBody(tokens, false);
		} else if (tokens.optional(T_SEMICOLON) != null) {
			return new ParsedEmptyFunctionBody(tokens.getPosition());
		} else {
			tokens.required(T_AOPEN, "{ expected");
			List<ParsedStatement> statements = new ArrayList<>();
			while (tokens.optional(T_ACLOSE) == null) {
				statements.add(ParsedStatement.parse(tokens));
			}
			return new ParsedStatementsFunctionBody(statements);
		}
	}
	
	public static ParsedStatement parse(ZSTokenStream parser) {
		ZSToken next = parser.peek();
		switch (next.getType()) {
			case T_AOPEN: {
				ZSToken t = parser.next();
				ArrayList<ParsedStatement> statements = new ArrayList<>();
				while (parser.optional(T_ACLOSE) == null) {
					statements.add(parse(parser));
				}
				return new ParsedStatementBlock(t.getPosition(), statements);
			}
			case K_RETURN: {
				parser.next();
				ParsedExpression expression = null;
				if (!parser.isNext(T_SEMICOLON)) {
					expression = ParsedExpression.parse(parser);
				}
				parser.required(T_SEMICOLON, "; expected");
				return new ParsedStatementReturn(next.getPosition(), expression);
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
				return new ParsedStatementVar(start.getPosition(), name, type, initializer, start.getType() == K_VAL);
			}
			case K_IF: {
				ZSToken t = parser.next();
				ParsedExpression expression = ParsedExpression.parse(parser);
				ParsedStatement onIf = parse(parser);
				ParsedStatement onElse = null;
				if (parser.optional(K_ELSE) != null) {
					onElse = parse(parser);
				}
				return new ParsedStatementIf(t.getPosition(), expression, onIf, onElse);
			}
			case K_FOR: {
				ZSToken t = parser.next();
				String name = parser.required(T_IDENTIFIER, "identifier expected").content;
				List<String> names = new ArrayList<>();
				names.add(name);

				while (parser.optional(T_COMMA) != null) {
					names.add(parser.required(T_IDENTIFIER, "identifier expected").content);
				}

				parser.required(K_IN, "in expected");
				ParsedExpression source = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);
				return new ParsedStatementForeach(
						t.getPosition(),
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
				return new ParsedStatementDoWhile(t.position, label, content, condition);
			}
			case K_WHILE: {
				ZSToken t = parser.next();
				String label = null;
				if (parser.optional(T_COLON) != null)
					label = parser.required(T_IDENTIFIER, "identifier expected").content;
				
				ParsedExpression condition = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);
				return new ParsedStatementWhile(t.position, label, condition, content);
			}
			case K_LOCK: {
				ZSToken t = parser.next();
				ParsedExpression object = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);
				return new ParsedStatementLock(t.position, object, content);
			}
			case K_THROW: {
				ZSToken t = parser.next();
				ParsedExpression value = ParsedExpression.parse(parser);
				parser.required(T_SEMICOLON, "; expected");
				return new ParsedStatementThrow(t.position, value);
			}
			case K_TRY: {
				ZSToken t = parser.next();
				
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
					String catchName = null;
					if (parser.isNext(ZSTokenType.T_IDENTIFIER))
						catchName = parser.next().content;
					
					IParsedType catchType = ParsedTypeBasic.ANY;
					if (parser.optional(K_AS) != null)
						catchType = IParsedType.parse(parser);
					
					ParsedStatement catchContent = ParsedStatement.parse(parser);
					catchClauses.add(new ParsedCatchClause(catchName, catchType, catchContent));
				}
				
				ParsedStatement finallyClause = null;
				if (parser.optional(K_FINALLY) != null)
					finallyClause = ParsedStatement.parse(parser);
				
				return new ParsedStatementTryCatch(t.position, name, initializer, content, catchClauses, finallyClause);
			}
			case K_CONTINUE: {
				ZSToken t = parser.next();
				String name = null;
				if (parser.isNext(T_IDENTIFIER))
					name = parser.next().content;
				
				parser.required(T_SEMICOLON, "; expected");
				return new ParsedStatementContinue(t.position, name);
			}
			case K_BREAK: {
				ZSToken t = parser.next();
				String name = null;
				if (parser.isNext(T_IDENTIFIER))
					name = parser.next().content;
				
				parser.required(T_SEMICOLON, "; expected");
				return new ParsedStatementBreak(t.position, name);
			}
		}

		CodePosition position = parser.peek().getPosition();
		ParsedStatementExpression result = new ParsedStatementExpression(position, ParsedExpression.parse(parser));
		parser.required(T_SEMICOLON, "; expected");
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

	public ParsedStatement(CodePosition position) {
		this.position = position;
	}

	public abstract Statement compile(StatementScope scope);
}
