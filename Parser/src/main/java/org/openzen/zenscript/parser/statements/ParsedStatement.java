package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.compilation.statement.CompilableStatement;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.WhitespacePostComment;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.ArrayList;
import java.util.List;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

public abstract class ParsedStatement implements CompilableStatement {
	public final CodePosition position;
	public final ParsedAnnotation[] annotations;
	public final WhitespaceInfo whitespace;

	public ParsedStatement(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace) {
		this.position = position;
		this.annotations = annotations;
		this.whitespace = whitespace;
	}

	public static ParsedFunctionBody parseLambdaBody(ZSTokenParser tokens, boolean inExpression) throws ParseException {
		CodePosition position = tokens.getPosition();
		if (tokens.optional(T_AOPEN) != null) {
			List<ParsedStatement> statements = new ArrayList<>();
			while (tokens.optional(T_ACLOSE) == null)
				statements.add(ParsedStatement.parse(tokens));

			return new ParsedStatementsFunctionBody(new ParsedStatementBlock(position, ParsedAnnotation.NONE, null, null, statements));
		} else {
			ParsedFunctionBody result = new ParsedLambdaFunctionBody(position, ParsedExpression.parse(tokens));
			if (!inExpression)
				tokens.required(T_SEMICOLON, "; expected");
			return result;
		}
	}

	public static ParsedFunctionBody parseFunctionBody(ZSTokenParser tokens) throws ParseException {
		if (tokens.optional(T_LAMBDA) != null)
			return parseLambdaBody(tokens, false);
		else if (tokens.optional(T_SEMICOLON) != null)
			return new ParsedEmptyFunctionBody(tokens.getPosition());
		else
			return new ParsedStatementsFunctionBody(parseBlock(tokens, ParsedAnnotation.NONE, true));
	}

	public static ParsedStatementBlock parseBlock(ZSTokenParser parser, ParsedAnnotation[] annotations, boolean isFirst) throws ParseException {
		String ws = parser.getLastWhitespace();
		CodePosition position = parser.getPosition();
		parser.required(T_AOPEN, "{ expected");
		parser.skipWhitespaceNewline();

		ArrayList<ParsedStatement> statements = new ArrayList<>();
		boolean firstContent = true;
		while (parser.optional(T_ACLOSE) == null) {
			statements.add(parse(parser, annotations, firstContent));
			firstContent = false;
		}

		WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
		WhitespacePostComment postComment = WhitespacePostComment.fromWhitespace(parser.getLastWhitespace());
		return new ParsedStatementBlock(position, annotations, whitespace, postComment, statements);
	}

	public static ParsedStatement parse(ZSTokenParser parser) throws ParseException {
		ParsedAnnotation[] annotations = ParsedAnnotation.parseAnnotations(parser);
		return parse(parser, annotations);
	}

	public static ParsedStatement parse(ZSTokenParser parser, ParsedAnnotation[] annotations) throws ParseException {
		return parse(parser, annotations, false);
	}

	public static ParsedStatement parse(ZSTokenParser parser, ParsedAnnotation[] annotations, boolean isFirst) throws ParseException {
		String ws = parser.getLastWhitespace();
		CodePosition position = parser.getPosition();
		ZSToken next = parser.peek();
		switch (next.getType()) {
			case T_AOPEN:
				return parseBlock(parser, annotations, isFirst);

			case K_RETURN: {
				parser.next();
				CompilableExpression expression = null;
				if (!parser.isNext(T_SEMICOLON)) {
					expression = ParsedExpression.parse(parser);
				}
				parser.required(T_SEMICOLON, "; expected");

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementReturn(position, annotations, whitespace, expression);
			}
			case K_VAR:
			case K_VAL: {
				ZSToken start = parser.next();
				String name = parser.required(T_IDENTIFIER, "identifier expected").content;

				IParsedType type = null;
				CompilableExpression initializer = null;
				if (parser.optional(K_AS) != null || parser.optional(T_COLON) != null) {
					type = IParsedType.parse(parser);
				}
				if (parser.optional(T_ASSIGN) != null) {
					initializer = ParsedExpression.parse(parser);
				}
				parser.required(T_SEMICOLON, "; expected");

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementVar(position, annotations, whitespace, name, type, initializer, start.getType() == K_VAL);
			}
			case K_IF: {
				parser.next();
				CompilableExpression expression = ParsedExpression.parse(parser);
				parser.skipWhitespaceNewline();
				ParsedStatement onIf = parse(parser);
				ParsedStatement onElse = null;
				if (parser.optional(K_ELSE) != null) {
					parser.skipWhitespaceNewline();
					onElse = parse(parser);
				}

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementIf(position, annotations, whitespace, expression, onIf, onElse);
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
				CompilableExpression source = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementForeach(
						position,
						annotations,
						whitespace,
						names.toArray(new String[0]),
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
				CompilableExpression condition = ParsedExpression.parse(parser);
				parser.required(T_SEMICOLON, "; expected");

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementDoWhile(position, annotations, whitespace, label, content, condition);
			}
			case K_WHILE: {
				ZSToken t = parser.next();
				String label = null;
				if (parser.optional(T_COLON) != null)
					label = parser.required(T_IDENTIFIER, "identifier expected").content;

				CompilableExpression condition = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementWhile(position, annotations, whitespace, label, condition, content);
			}
			case K_LOCK: {
				ZSToken t = parser.next();
				CompilableExpression object = ParsedExpression.parse(parser);
				ParsedStatement content = parse(parser);

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementLock(position, annotations, whitespace, object, content);
			}
			case K_THROW: {
				ZSToken t = parser.next();
				CompilableExpression value = ParsedExpression.parse(parser);
				parser.required(T_SEMICOLON, "; expected");

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementThrow(position, annotations, whitespace, value);
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
				CompilableExpression initializer = null;

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

					ParsedStatement catchContent = ParsedStatement.parse(parser);
					catchClauses.add(new ParsedCatchClause(catchPosition, catchName, catchContent));
				}

				ParsedStatement finallyClause = null;
				if (parser.optional(K_FINALLY) != null)
					finallyClause = ParsedStatement.parse(parser);

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementTryCatch(position, annotations, whitespace, name, initializer, content, catchClauses, finallyClause);
			}
			case K_CONTINUE: {
				ZSToken t = parser.next();
				String name = null;
				if (parser.isNext(T_IDENTIFIER))
					name = parser.next().content;

				parser.required(T_SEMICOLON, "; expected");

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementContinue(position, annotations, whitespace, name);
			}
			case K_BREAK: {
				ZSToken t = parser.next();
				String name = null;
				if (parser.isNext(T_IDENTIFIER))
					name = parser.next().content;

				parser.required(T_SEMICOLON, "; expected");

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementBreak(position, annotations, whitespace, name);
			}
			case K_SWITCH: {
				ZSToken t = parser.next();
				String name = null;
				if (parser.optional(T_COLON) != null)
					name = parser.next().content;

				CompilableExpression value = ParsedExpression.parse(parser);
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
						throw new ParseException(parser.getPosition(), "Statement in switch outside case");
					} else {
						currentCase.statements.add(ParsedStatement.parse(parser));
					}
				}

				WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);
				return new ParsedStatementSwitch(position, annotations, whitespace, name, value, cases);
			}
		}

		CompilableExpression expression = ParsedExpression.parse(parser);
		parser.required(T_SEMICOLON, "; expected");
		WhitespaceInfo whitespace = parser.collectWhitespaceInfo(ws, isFirst);

		ParsedStatementExpression result = new ParsedStatementExpression(position, annotations, whitespace, expression);
		return result;
	}

	protected Statement result(Statement statement, StatementCompiler compiler) {
		statement.setTag(WhitespaceInfo.class, whitespace);
		statement.annotations = ParsedAnnotation.compileForStatement(annotations, statement, compiler);
		return statement;
	}
}
