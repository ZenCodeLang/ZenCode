package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.expression.CompilableExpression;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.openzen.zencode.shared.StringExpansion.unescape;
import static org.openzen.zenscript.lexer.ZSTokenType.*;

public abstract class ParsedExpression implements CompilableExpression {
	public final CodePosition position;

	public ParsedExpression(CodePosition position) {
		this.position = position;
	}

	public static ParsedExpression parse(ZSTokenParser parser) throws ParseException {
		return readAssignExpression(parser, ParsingOptions.DEFAULT);
	}

	public static ParsedExpression parse(ZSTokenParser parser, ParsingOptions options) throws ParseException {
		return readAssignExpression(parser, options);
	}

	private static ParsedExpression readAssignExpression(ZSTokenParser parser, ParsingOptions options) throws ParseException {
		CodePosition position = parser.getPosition();
		ParsedExpression left = readConditionalExpression(position, parser, options);

		switch (parser.peek().type) {
			case T_ASSIGN:
				parser.next();
				return new ParsedExpressionAssign(position, left, readAssignExpression(parser, options));
			case T_ADDASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.ADDASSIGN);
			case T_SUBASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.SUBASSIGN);
			case T_CATASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.CATASSIGN);
			case T_MULASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.MULASSIGN);
			case T_DIVASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.DIVASSIGN);
			case T_MODASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.MODASSIGN);
			case T_ORASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.ORASSIGN);
			case T_ANDASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.ANDASSIGN);
			case T_XORASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.XORASSIGN);
			case T_SHLASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.SHLASSIGN);
			case T_SHRASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.SHRASSIGN);
			case T_USHRASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser, options), OperatorType.USHRASSIGN);
		}

		return left;
	}

	private static ParsedExpression readConditionalExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readOrOrExpression(position, parser, options);

		if (parser.optional(T_QUEST) != null) {
			ParsedExpression onIf = readOrOrExpression(parser.getPosition(), parser, options);
			parser.required(T_COLON, ": expected");
			ParsedExpression onElse = readConditionalExpression(parser.getPosition(), parser, options);
			return new ParsedExpressionConditional(position, left, onIf, onElse);
		}

		return left;
	}

	private static ParsedExpression readOrOrExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readAndAndExpression(position, parser, options);

		while (parser.optional(T_OROR) != null) {
			ParsedExpression right = readAndAndExpression(parser.getPosition(), parser, options);
			left = new ParsedExpressionOrOr(position, left, right);
		}

		while (parser.optional(T_COALESCE) != null) {
			ParsedExpression right = readAndAndExpression(parser.getPosition(), parser, options);
			left = new ParsedExpressionCoalesce(position, left, right);
		}

		return left;
	}

	private static ParsedExpression readAndAndExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readOrExpression(position, parser, options);

		while (parser.optional(T_ANDAND) != null) {
			ParsedExpression right = readOrExpression(parser.getPosition(), parser, options);
			left = new ParsedExpressionAndAnd(position, left, right);
		}
		return left;
	}

	private static ParsedExpression readOrExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readXorExpression(position, parser, options);

		while (parser.optional(T_OR) != null) {
			ParsedExpression right = readXorExpression(parser.getPosition(), parser, options);
			left = new ParsedExpressionBinary(position, left, right, OperatorType.OR);
		}
		return left;
	}

	private static ParsedExpression readXorExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readAndExpression(position, parser, options);

		while (parser.optional(T_XOR) != null) {
			ParsedExpression right = readAndExpression(parser.getPosition(), parser, options);
			left = new ParsedExpressionBinary(position, left, right, OperatorType.XOR);
		}
		return left;
	}

	private static ParsedExpression readAndExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readCompareExpression(position, parser, options);

		while (parser.optional(T_AND) != null) {
			ParsedExpression right = readCompareExpression(parser.getPosition(), parser, options);
			left = new ParsedExpressionBinary(position, left, right, OperatorType.AND);
		}
		return left;
	}

	private static ParsedExpression readCompareExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readShiftExpression(position, parser, options);

		switch (parser.peek().getType()) {
			case T_EQUAL2: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionCompare(position, left, right, CompareType.EQ);
			}
			case T_EQUAL3: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionSame(position, left, right, false);
			}
			case T_NOTEQUAL: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionCompare(position, left, right, CompareType.NE);
			}
			case T_NOTEQUAL2: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionSame(position, left, right, true);
			}
			case T_LESS: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionCompare(position, left, right, CompareType.LT);
			}
			case T_LESSEQ: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionCompare(position, left, right, CompareType.LE);
			}
			case T_GREATER: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionCompare(position, left, right, CompareType.GT);
			}
			case T_GREATEREQ: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionCompare(position, left, right, CompareType.GE);
			}
			case K_IN: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
				return new ParsedExpressionBinary(position, right, left, OperatorType.CONTAINS);
			}
			case K_IS: {
				parser.next();
				IParsedType type = IParsedType.parse(parser);
				return new ParsedExpressionIs(position, left, type);
			}
			case T_NOT: {
				parser.next();
				if (parser.optional(K_IN) != null) {
					ParsedExpression right = readShiftExpression(parser.getPosition(), parser, options);
					return new ParsedExpressionUnary(position, new ParsedExpressionBinary(position, right, left, OperatorType.CONTAINS), OperatorType.NOT);
				} else if (parser.optional(K_IS) != null) {
					IParsedType type = IParsedType.parse(parser);
					return new ParsedExpressionUnary(position, new ParsedExpressionIs(position, left, type), OperatorType.NOT);
				} else {
					throw new ParseException(position, "Expected in or is");
				}
			}
		}

		return left;
	}

	private static ParsedExpression readShiftExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readAddExpression(position, parser, options);

		while (true) {
			if (parser.optional(T_SHL) != null) {
				ParsedExpression right = readAddExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.SHL);
			} else if (parser.optional(T_SHR) != null) {
				ParsedExpression right = readAddExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.SHR);
			} else if (parser.optional(T_USHR) != null) {
				ParsedExpression right = readAddExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.USHR);
			} else {
				break;
			}
		}

		return left;
	}

	private static ParsedExpression readAddExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readMulExpression(position, parser, options);

		while (true) {
			if (parser.optional(T_ADD) != null) {
				ParsedExpression right = readMulExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.ADD);
			} else if (parser.optional(T_SUB) != null) {
				ParsedExpression right = readMulExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.SUB);
			} else if (parser.optional(T_CAT) != null) {
				ParsedExpression right = readMulExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.CAT);
			} else {
				// Check if x-1 was scanned as T_INT instead of [T_SUB, T_INT]
				// If so, replace the Token with the number and treat it as binary call
				final ZSToken peek = parser.peek();
				if (peek.content.startsWith("-") && !peek.content.equals("-=") && peek.content.length() >= 2) {
					parser.replace(new ZSToken(peek.type, peek.content.substring(1)));
					ParsedExpression right = readMulExpression(parser.getPosition(), parser, options);
					left = new ParsedExpressionBinary(position, left, right, OperatorType.SUB);
				} else {
					break;
				}

			}
		}
		return left;
	}

	private static ParsedExpression readMulExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression left = readUnaryExpression(position, parser, options);

		while (true) {
			if (parser.optional(T_MUL) != null) {
				ParsedExpression right = readUnaryExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.MUL);
			} else if (parser.optional(T_DIV) != null) {
				ParsedExpression right = readUnaryExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.DIV);
			} else if (parser.optional(T_MOD) != null) {
				ParsedExpression right = readUnaryExpression(parser.getPosition(), parser, options);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.MOD);
			} else {
				break;
			}
		}

		return left;
	}

	private static ParsedExpression readUnaryExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		switch (parser.peek().getType()) {
			case T_NOT:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.getPosition(), parser, options),
						OperatorType.NOT);
			case T_SUB:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.getPosition(), parser, options),
						OperatorType.NEG);
			case T_CAT:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.getPosition(), parser, options),
						OperatorType.CAT);
			case T_INCREMENT:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.getPosition(), parser, options),
						OperatorType.INCREMENT);
			case T_DECREMENT:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.getPosition(), parser, options),
						OperatorType.DECREMENT);
			case K_TRY:
				parser.next();
				if (parser.optional(T_QUEST) != null) {
					// try? - attempts the specified operation returning T and throwing E. The result is converted to Result<T, E> depending on success or failure
					return new ParsedTryConvertExpression(position, readUnaryExpression(position, parser, options));
				} else if (parser.optional(T_NOT) != null) {
					// try! - attempts the specified operation
					// a) if the operation throws E and returns T
					//		- if the operation succeeds, returns T
					//		- if the operation thows and the function throws, will attempt to convert E to the thrown type and throw it
					//		- if the operation throws and the function returns Result<?, X>, will attempt to convert E to X and return a failure
					// b) if the operation returns Result<T, E>
					//		- if the operation succeeds, returns T
					//		- if the operation fails and the function throws, will attempt to convert E to the thrown type and throw it
					//		- if the operation fails and the function returns Result<?, X>, will attempt to convert E to X and return a failure
					// try! can thus be used to convert from exception-based to result-based and vice versa
					return new ParsedTryRethrowExpression(position, readUnaryExpression(position, parser, options));
				}
			default:
				return readPostfixExpression(position, parser, options);
		}
	}

	private static ParsedExpression readPostfixExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		ParsedExpression base = readPrimaryExpression(position, parser, options);

		while (true) {
			if (parser.optional(T_DOT) != null) {
				ZSToken indexString = parser.optional(T_IDENTIFIER);
				if (indexString != null) {
					List<IParsedType> genericParameters = IParsedType.parseTypeArguments(parser);
					base = new ParsedExpressionMember(position.until(parser.getPositionBeforeWhitespace()), base, indexString.content, genericParameters);
				} else if (parser.optional(T_DOLLAR) != null) {
					base = new ParsedExpressionOuter(position.until(parser.getPositionBeforeWhitespace()), base);
				} else {
					ZSToken indexString2 = parser.optional(T_STRING_SQ);
					if (indexString2 == null)
						indexString2 = parser.optional(T_STRING_DQ);

					if (indexString2 != null) {
						// TODO: handle this properly
						base = new ParsedExpressionMember(position.until(parser.getPositionBeforeWhitespace()), base, unescape(indexString2.content).orElse("INVALID STRING"), Collections.emptyList());
					} else {
						position = parser.getPosition();
						ZSToken last = parser.next();
						throw new ParseException(position.until(parser.getPositionBeforeWhitespace()), "Invalid expression, last token: " + last.content);
					}
				}
			} else if (parser.optional(T_DOT2) != null) {
				ParsedExpression to = readAssignExpression(parser, options);
				return new ParsedExpressionRange(position.until(parser.getPositionBeforeWhitespace()), base, to);
			} else if (parser.optional(T_SQOPEN) != null) {
				List<ParsedExpression> indexes = new ArrayList<>();
				do {
					indexes.add(readAssignExpression(parser, options));
				} while (parser.optional(ZSTokenType.T_COMMA) != null);
				parser.required(T_SQCLOSE, "] expected");
				base = new ParsedExpressionIndex(position, base, indexes);
			} else if (parser.isNext(T_BROPEN)) {
				base = new ParsedExpressionCall(position, base, ParsedCallArguments.parse(parser));
			} else if (parser.optional(K_AS) != null) {
				boolean optional = parser.optional(T_QUEST) != null;
				IParsedType type = IParsedType.parse(parser);
				base = new ParsedExpressionCast(position, base, type, optional);
			} else if (parser.optional(T_INCREMENT) != null) {
				base = new ParsedExpressionPostCall(position, base, OperatorType.INCREMENT);
			} else if (parser.optional(T_DECREMENT) != null) {
				base = new ParsedExpressionPostCall(position, base, OperatorType.DECREMENT);
			} else if (options.allowLambda && parser.optional(T_LAMBDA) != null) {
				ParsedFunctionBody body = ParsedStatement.parseLambdaBody(parser, true);
				base = new ParsedExpressionFunction(position, base.toLambdaHeader(), body);
			} else {
				break;
			}
		}

		return base;
	}

	private static ParsedExpression readPrimaryExpression(CodePosition position, ZSTokenParser parser, ParsingOptions options) throws ParseException {
		switch (parser.peek().getType()) {
			case T_INT:
				return new ParsedExpressionInt(position, parser.next().content);
			case T_PREFIXED_INT:
				return ParsedExpressionInt.parsePrefixed(position, parser.next().content);
			case T_FLOAT:
				return new ParsedExpressionFloat(
						position,
						parser.next().content);
			case T_STRING_SQ:
			case T_STRING_DQ: {
				String quoted = parser.next().content;
				return new ParsedExpressionString(
						position,
						StringExpansion.unescape(quoted).orElse(error -> {
							return "INVALID_STRING";
						}),
						quoted.charAt(0) == '\'');
			}
			case T_IDENTIFIER: {
				String name = parser.next().content;
				if (name.startsWith("@"))
					name = name.substring(1);

				List<IParsedType> genericParameters = IParsedType.parseTypeArguments(parser);
				return new ParsedExpressionVariable(position.until(parser.getPositionBeforeWhitespace()), name, genericParameters);
			}
			case T_LOCAL_IDENTIFIER: {
				String name = parser.next().content.substring(1);
				return new ParsedLocalVariableExpression(position, name);
			}
			case K_THIS:
				parser.next();
				return new ParsedExpressionThis(position);
			case K_SUPER:
				parser.next();
				return new ParsedExpressionSuper(position);
			case T_DOLLAR:
				parser.next();
				return new ParsedDollarExpression(position);
			case T_SQOPEN: {
				parser.next();
				List<ParsedExpression> contents = new ArrayList<>();
				if (parser.optional(T_SQCLOSE) == null) {
					while (parser.optional(T_SQCLOSE) == null) {
						contents.add(readAssignExpression(parser, options));
						if (parser.optional(T_COMMA) == null) {
							parser.required(T_SQCLOSE, "] or , expected");
							break;
						}
					}
				}
				return new ParsedExpressionArray(position, contents);
			}
			case T_AOPEN: {
				parser.next();

				List<ParsedExpression> keys = new ArrayList<>();
				List<ParsedExpression> values = new ArrayList<>();
				while (parser.optional(T_ACLOSE) == null) {
					ParsedExpression expression = readAssignExpression(parser, options);
					if (parser.optional(T_COLON) == null) {
						keys.add(null);
						values.add(expression);
					} else {
						keys.add(expression);
						values.add(readAssignExpression(parser, options));
					}

					if (parser.optional(T_COMMA) == null) {
						parser.required(T_ACLOSE, "} or , expected");
						break;
					}
				}
				return new ParsedExpressionMap(position, keys, values);
			}
			case K_TRUE:
				parser.next();
				return new ParsedExpressionBool(position, true);
			case K_FALSE:
				parser.next();
				return new ParsedExpressionBool(position, false);
			case K_NULL:
				parser.next();
				return new ParsedExpressionNull(position);
			case T_BROPEN: {
				parser.next();
				List<ParsedExpression> expressions = new ArrayList<>();
				do {
					if (parser.peek().type == T_BRCLOSE) {
						break;
					}
					expressions.add(readAssignExpression(parser, options));
				} while (parser.optional(ZSTokenType.T_COMMA) != null);
				parser.required(T_BRCLOSE, ") expected");
				return new ParsedExpressionBracket(position, expressions);
			}
			case K_NEW: {
				parser.next();
				IParsedType type = IParsedType.parse(parser);
				ParsedCallArguments newArguments = ParsedCallArguments.NONE;
				if (parser.isNext(ZSTokenType.T_BROPEN) || parser.isNext(ZSTokenType.T_LESS))
					newArguments = ParsedCallArguments.parse(parser);

				return new ParsedNewExpression(position, type, newArguments);
			}
			case K_THROW: {
				parser.next();
				ParsedExpression value = parse(parser);
				return new ParsedThrowExpression(position, value);
			}
			case K_PANIC: {
				parser.next();
				ParsedExpression value = parse(parser);
				return new ParsedPanicExpression(position, value);
			}
			case K_MATCH: {
				parser.next();
				ParsedExpression source = parse(parser);
				parser.required(T_AOPEN, "{ expected");

				List<ParsedMatchExpression.Case> cases = new ArrayList<>();
				while (parser.optional(T_ACLOSE) == null) {
					ParsedExpression key = null;
					if (parser.optional(K_DEFAULT) == null)
						key = parse(parser, new ParsingOptions(false));

					parser.required(T_LAMBDA, "=> expected");
					ParsedExpression value = parse(parser);
					cases.add(new ParsedMatchExpression.Case(key, value));

					if (parser.optional(T_COMMA) == null)
						break;
				}
				parser.required(T_ACLOSE, "} expected");
				return new ParsedMatchExpression(position, source, cases);
			}
			case T_LESS:// bracket expression
				parser.next();
				if (parser.bracketParser == null)
					throw new ParseException(position, "Bracket expression detected but no bracket parser present");

				return parser.bracketParser.parse(position, parser);
			default: {
				IParsedType type = IParsedType.parse(parser);
				if (type == null) {
					ZSToken last = parser.next();
					throw new ParseException(parser.getPosition(), "Invalid expression, last token: " + last.content);
				} else {
					return new ParsedTypeExpression(position, type);
				}
			}
		}
	}

	public SwitchValue compileToSwitchValue(TypeID type, ExpressionScope scope) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");
	}

	public ParsedFunctionHeader toLambdaHeader() throws ParseException {
		throw new ParseException(position, "Not a valid lambda header");
	}

	public ParsedFunctionParameter toLambdaParameter() throws ParseException {
		throw new ParseException(position, "Not a valid lambda parameter");
	}

	public boolean isCompatibleWith(BaseScope scope, TypeID type) {
		return true;
	}

	public static class ParsingOptions {
		public static final ParsingOptions DEFAULT = new ParsingOptions(true);

		public final boolean allowLambda;

		public ParsingOptions(boolean allowLambda) {
			this.allowLambda = allowLambda;
		}
	}
}
