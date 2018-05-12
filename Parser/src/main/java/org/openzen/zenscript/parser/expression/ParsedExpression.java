/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.linker.ExpressionScope;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.ParseException;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import static org.openzen.zenscript.shared.StringUtils.unescape;

/**
 *
 * @author Stanneke
 */
public abstract class ParsedExpression {
	public static ParsedExpression parse(ZSTokenStream parser) {
		return readAssignExpression(parser);
	}

	private static ParsedExpression readAssignExpression(ZSTokenStream parser) {
		CodePosition position = parser.getPosition();
		ParsedExpression left = readConditionalExpression(position, parser);

		switch (parser.peek().type) {
			case T_ASSIGN:
				parser.next();
				return new ParsedExpressionAssign(position, left, readAssignExpression(parser));
			case T_ADDASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.ADDASSIGN);
			case T_SUBASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.SUBASSIGN);
			case T_CATASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.CATASSIGN);
			case T_MULASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.MULASSIGN);
			case T_DIVASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.DIVASSIGN);
			case T_MODASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.MODASSIGN);
			case T_ORASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.ORASSIGN);
			case T_ANDASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.ANDASSIGN);
			case T_XORASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.XORASSIGN);
			case T_SHLASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.SHLASSIGN);
			case T_SHRASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.SHRASSIGN);
			case T_USHRASSIGN:
				parser.next();
				return new ParsedExpressionOpAssign(position, left, readAssignExpression(parser), OperatorType.USHRASSIGN);
		}

		return left;
	}

	private static ParsedExpression readConditionalExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readOrOrExpression(position, parser);

		if (parser.optional(T_QUEST) != null) {
			ParsedExpression onIf = readOrOrExpression(parser.peek().position, parser);
			parser.required(T_COLON, ": expected");
			ParsedExpression onElse = readConditionalExpression(parser.peek().position, parser);
			return new ParsedExpressionConditional(position, left, onIf, onElse);
		}

		return left;
	}

	private static ParsedExpression readOrOrExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readAndAndExpression(position, parser);

		while (parser.optional(T_OROR) != null) {
			ParsedExpression right = readAndAndExpression(parser.peek().position, parser);
			left = new ParsedExpressionOrOr(position, left, right);
		}
		
		while (parser.optional(T_COALESCE) != null) {
			ParsedExpression right = readAndAndExpression(parser.peek().position, parser);
			left = new ParsedExpressionCoalesce(position, left, right);
		}
		
		return left;
	}

	private static ParsedExpression readAndAndExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readOrExpression(position, parser);

		while (parser.optional(T_ANDAND) != null) {
			ParsedExpression right = readOrExpression(parser.peek().position, parser);
			left = new ParsedExpressionAndAnd(position, left, right);
		}
		return left;
	}

	private static ParsedExpression readOrExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readXorExpression(position, parser);

		while (parser.optional(T_OR) != null) {
			ParsedExpression right = readXorExpression(parser.peek().position, parser);
			left = new ParsedExpressionBinary(position, left, right, OperatorType.OR);
		}
		return left;
	}

	private static ParsedExpression readXorExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readAndExpression(position, parser);

		while (parser.optional(T_XOR) != null) {
			ParsedExpression right = readAndExpression(parser.peek().position, parser);
			left = new ParsedExpressionBinary(position, left, right, OperatorType.XOR);
		}
		return left;
	}

	private static ParsedExpression readAndExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readCompareExpression(position, parser);

		while (parser.optional(T_AND) != null) {
			ParsedExpression right = readCompareExpression(parser.peek().position, parser);
			left = new ParsedExpressionBinary(position, left, right, OperatorType.AND);
		}
		return left;
	}

	private static ParsedExpression readCompareExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readShiftExpression(position, parser);

		switch (parser.peek().getType()) {
			case T_EQUAL2: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
				return new ParsedExpressionCompare(position, left, right, CompareType.EQ);
			}
			case T_EQUAL3: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
				return new ParsedExpressionCompare(position, left, right, CompareType.SAME);
			}
			case T_NOTEQUAL: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
				return new ParsedExpressionCompare(position, left, right, CompareType.NE);
			}
			case T_NOTEQUAL2: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
				return new ParsedExpressionCompare(position, left, right, CompareType.NOTSAME);
			}
			case T_LESS: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
				return new ParsedExpressionCompare(position, left, right, CompareType.LT);
			}
			case T_LESSEQ: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
				return new ParsedExpressionCompare(position, left, right, CompareType.LE);
			}
			case T_GREATER: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
				return new ParsedExpressionCompare(position, left, right, CompareType.GT);
			}
			case T_GREATEREQ: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
				return new ParsedExpressionCompare(position, left, right, CompareType.GE);
			}
			case K_IN: {
				parser.next();
				ParsedExpression right = readShiftExpression(parser.peek().position, parser);
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
					ParsedExpression right = readShiftExpression(parser.peek().position, parser);
					return new ParsedExpressionUnary(position, new ParsedExpressionBinary(position, right, left, OperatorType.CONTAINS), OperatorType.NOT);
				} else if (parser.optional(K_IS) != null) {
					IParsedType type = IParsedType.parse(parser);
					return new ParsedExpressionUnary(position, new ParsedExpressionIs(position, left, type), OperatorType.NOT);
				} else {
					throw new CompileException(position, CompileExceptionCode.UNEXPECTED_TOKEN, "Expected in or is");
				}
			}
		}

		return left;
	}
	
	private static ParsedExpression readShiftExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readAddExpression(position, parser);
		
		while (true) {
			if (parser.optional(T_SHL) != null) {
				ParsedExpression right = readAddExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.SHL);
			} else if (parser.optional(T_SHR) != null) {
				ParsedExpression right = readAddExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.SHR);
			} else if (parser.optional(T_USHR) != null) {
				ParsedExpression right = readAddExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.USHR);
			} else {
				break;
			}
		}
		
		return left;
	}

	private static ParsedExpression readAddExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readMulExpression(position, parser);
		
		while (true) {
			if (parser.optional(T_ADD) != null) {
				ParsedExpression right = readMulExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.ADD);
			} else if (parser.optional(T_SUB) != null) {
				ParsedExpression right = readMulExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.SUB);
			} else if (parser.optional(T_CAT) != null) {
				ParsedExpression right = readMulExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.CAT);
			} else {
				break;
			}
		}
		return left;
	}

	private static ParsedExpression readMulExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression left = readUnaryExpression(position, parser);

		while (true) {
			if (parser.optional(T_MUL) != null) {
				ParsedExpression right = readUnaryExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.MUL);
			} else if (parser.optional(T_DIV) != null) {
				ParsedExpression right = readUnaryExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.DIV);
			} else if (parser.optional(T_MOD) != null) {
				ParsedExpression right = readUnaryExpression(parser.peek().position, parser);
				left = new ParsedExpressionBinary(position, left, right, OperatorType.MOD);
			} else {
				break;
			}
		}

		return left;
	}

	private static ParsedExpression readUnaryExpression(CodePosition position, ZSTokenStream parser) {
		switch (parser.peek().getType()) {
			case T_NOT:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.peek().position, parser),
						OperatorType.NOT);
			case T_SUB:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.peek().position, parser),
						OperatorType.NEG);
			case T_CAT:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.peek().position, parser),
						OperatorType.CAT);
			case T_INCREMENT:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.peek().position, parser),
						OperatorType.INCREMENT);
			case T_DECREMENT:
				parser.next();
				return new ParsedExpressionUnary(
						position,
						readUnaryExpression(parser.peek().position, parser),
						OperatorType.DECREMENT);
			default:
				return readPostfixExpression(position, parser);
		}
	}

	private static ParsedExpression readPostfixExpression(CodePosition position, ZSTokenStream parser) {
		ParsedExpression base = readPrimaryExpression(position, parser);

		while (true) {
			if (parser.optional(T_DOT) != null) {
				ZSToken indexString = parser.optional(T_IDENTIFIER);
				if (indexString != null) {
					List<IParsedType> genericParameters = IParsedType.parseGenericParameters(parser);
					base = new ParsedExpressionMember(position, base, indexString.content, genericParameters);
				} else if (parser.optional(T_DOLLAR) != null) {
					base = new ParsedExpressionOuter(position, base);
				} else {
					ZSToken indexString2 = parser.optional(T_STRING_SQ);
					if (indexString2 == null)
						indexString2 = parser.optional(T_STRING_DQ);
					
					if (indexString2 != null) {
						base = new ParsedExpressionMember(position, base, unescape(indexString2.content), Collections.emptyList());
					} else {
						ZSToken last = parser.next();
						throw new ParseException(last, "Invalid expression, last token: " + last.content);
					}
				}
			} else if (parser.optional(T_DOT2) != null) {
				ParsedExpression to = readAssignExpression(parser);
				return new ParsedExpressionRange(position, base, to);
			} else if (parser.optional(T_SQOPEN) != null) {
				List<ParsedExpression> indexes = new ArrayList<>();
				do {
					indexes.add(readAssignExpression(parser));
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
			} else if (parser.optional(T_LAMBDA) != null) {
				ParsedFunctionBody body = ParsedStatement.parseLambdaBody(parser, true);
				base = new ParsedExpressionFunction(position, base.toLambdaHeader(), body);
			} else {
				break;
			}
		}

		return base;
	}
	
	private static ParsedExpression readPrimaryExpression(CodePosition position, ZSTokenStream parser) {
		switch (parser.peek().getType()) {
			case T_INT:
				return new ParsedExpressionInt(position, Long.parseLong(parser.next().content));
			case T_FLOAT:
				return new ParsedExpressionFloat(
						position,
						Double.parseDouble(parser.next().content));
			case T_STRING_SQ:
			case T_STRING_DQ:
				return new ParsedExpressionString(
						position,
						unescape(parser.next().content));
			case T_IDENTIFIER: {
				String name = parser.next().content;
				List<IParsedType> genericParameters = IParsedType.parseGenericParameters(parser);
				return new ParsedExpressionVariable(position, name, genericParameters);
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
			case T_LESS:
				throw new CompileException(position, CompileExceptionCode.UNSUPPORTED_XML_EXPRESSIONS, "XML expressions are not supported in ZSBootstrap");
			case T_SQOPEN: {
				parser.next();
				List<ParsedExpression> contents = new ArrayList<>();
				if (parser.optional(T_SQCLOSE) == null) {
					while (parser.optional(T_SQCLOSE) == null) {
						contents.add(readAssignExpression(parser));
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
					ParsedExpression expression = readAssignExpression(parser);
					if (parser.optional(T_COLON) == null) {
						keys.add(null);
						values.add(expression);
					} else {
						keys.add(expression);
						values.add(readAssignExpression(parser));
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
					expressions.add(readAssignExpression(parser));
				} while (parser.optional(ZSTokenType.T_COMMA) != null);
				parser.required(T_BRCLOSE, ") expected");
				return new ParsedExpressionBracket(position, expressions);
			}
			case K_NEW: {
				parser.next();
				IParsedType type = IParsedType.parse(parser);
				ParsedCallArguments newArguments = ParsedCallArguments.NONE;
				if (parser.isNext(ZSTokenType.T_BROPEN))
					newArguments = ParsedCallArguments.parse(parser);
				
				return new ParsedNewExpression(position, type, newArguments);
			}
			default: {
				IParsedType type = IParsedType.parse(parser);
				if (type == null) {
					ZSToken last = parser.next();
					throw new ParseException(last, "Invalid expression, last token: " + last.content);
				} else {
					return new ParsedTypeExpression(position, type);
				}
			}
		}
	}
	
	public final CodePosition position;
	
	public ParsedExpression(CodePosition position) {
		this.position = position;
	}
	
	/**
	 * Compiles the given parsed expression to a high-level expression or
	 * partial expression.
	 * 
	 * If the asType parameter is provided, the given type determines the output
	 * type of the expression. The output type of the expression MUST in that
	 * case be equal to the given type.
	 * 
	 * @param scope
	 * @return 
	 */
	public abstract IPartialExpression compile(ExpressionScope scope);

	public Expression compileKey(ExpressionScope scope) {
		return compile(scope).eval();
	}
	
	public ParsedFunctionHeader toLambdaHeader() {
		throw new ParseException(position, "Not a valid lambda header");
	}
	
	public ParsedFunctionParameter toLambdaParameter() {
		throw new ParseException(position, "Not a valid lambda parameter");
	}
	
	public boolean isCompatibleWith(BaseScope scope, ITypeID type) {
		return true;
	}
	
	public abstract boolean hasStrongType();
}
