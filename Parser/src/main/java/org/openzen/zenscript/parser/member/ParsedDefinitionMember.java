package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.parser.statements.ParsedStatementBlock;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedBasicType;

public abstract class ParsedDefinitionMember implements CompilableMember {
	public final ParsedAnnotation[] annotations;

	public ParsedDefinitionMember(ParsedAnnotation[] annotations) {
		this.annotations = annotations;
	}

	public static ParsedDefinitionMember parse(ZSTokenParser tokens) throws ParseException {
		CodePosition start = tokens.getPosition();
		ParsedAnnotation[] annotations = ParsedAnnotation.parseAnnotations(tokens);
		int modifiers = 0;
		outer:
		while (true) {
			switch (tokens.peek().type) {
				case K_INTERNAL:
					tokens.next();
					modifiers |= Modifiers.FLAG_INTERNAL;
					break;
				case K_PUBLIC:
					tokens.next();
					modifiers |= Modifiers.FLAG_PUBLIC;
					break;
				case K_PRIVATE:
					tokens.next();
					modifiers |= Modifiers.FLAG_PRIVATE;
					break;
				case K_CONST:
					tokens.next();
					if (tokens.optional(ZSTokenType.T_QUEST) != null) {
						modifiers |= Modifiers.FLAG_CONST_OPTIONAL;
					} else {
						modifiers |= Modifiers.FLAG_CONST;
					}
					break;
				case K_ABSTRACT:
					tokens.next();
					modifiers |= Modifiers.FLAG_ABSTRACT;
					break;
				case K_FINAL:
					tokens.next();
					modifiers |= Modifiers.FLAG_FINAL;
					break;
				case K_STATIC:
					tokens.next();
					modifiers |= Modifiers.FLAG_STATIC;
					break;
				case K_PROTECTED:
					tokens.next();
					modifiers |= Modifiers.FLAG_PROTECTED;
					break;
				case K_IMPLICIT:
					tokens.next();
					modifiers |= Modifiers.FLAG_IMPLICIT;
					break;
				case K_EXTERN:
					tokens.next();
					modifiers |= Modifiers.FLAG_EXTERN;
					break;
				case K_OVERRIDE:
					tokens.next();
					modifiers |= Modifiers.FLAG_OVERRIDE;
					break;
				default:
					break outer;
			}
		}

		switch (tokens.peek().type) {
			case K_VAL:
			case K_VAR: {
				ZSToken t = tokens.next();
				String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
				IParsedType type = ParsedBasicType.UNDETERMINED;
				if (tokens.optional(ZSTokenType.K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				CompilableExpression initializer = null;
				int autoGetter = 0;
				int autoSetter = 0;
				if (tokens.optional(ZSTokenType.T_COLON) != null) {
					do {
						int accessor = Modifiers.FLAG_PUBLIC;
						if (tokens.optional(ZSTokenType.K_PUBLIC) != null) {
							accessor = Modifiers.FLAG_PUBLIC;
						} else if (tokens.optional(ZSTokenType.K_PROTECTED) != null) {
							accessor = Modifiers.FLAG_PROTECTED;
						}

						if (tokens.optional(ZSTokenType.K_GET) != null) {
							autoGetter = accessor;
						} else {
							tokens.required(ZSTokenType.K_SET, "get or set expected");
							autoSetter = accessor;
						}
					} while (tokens.optional(ZSTokenType.T_COMMA) != null);
				}
				if (tokens.optional(ZSTokenType.T_ASSIGN) != null) {
					initializer = ParsedExpression.parse(tokens);
				}
				tokens.required(ZSTokenType.T_SEMICOLON, "; expected");
				return new ParsedField(start, modifiers, annotations, name, type, initializer, t.type == ZSTokenType.K_VAL, autoGetter, autoSetter);
			}
			case K_THIS: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				if (body == null)
					throw new ParseException(start, "Function body is required for constructors");

				return new ParsedConstructor(start, modifiers, annotations, header, body);
			}
			case T_IDENTIFIER: {
				String name = tokens.next().content;
				if ((modifiers & Modifiers.FLAG_CONST) == Modifiers.FLAG_CONST && (tokens.isNext(ZSTokenType.K_AS) || tokens.isNext(ZSTokenType.T_ASSIGN))) {
					IParsedType type = ParsedBasicType.UNDETERMINED;
					if (tokens.optional(ZSTokenType.K_AS) != null) {
						type = IParsedType.parse(tokens);
					}
					tokens.required(ZSTokenType.T_ASSIGN, "= expected");
					CompilableExpression value = ParsedExpression.parse(tokens);
					tokens.required(ZSTokenType.T_SEMICOLON, "; expected");
					return new ParsedConst(start, modifiers & ~Modifiers.FLAG_CONST, annotations, name, type, value);
				}

				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedMethod(start, modifiers, annotations, name, header, body);
			}
			case K_SET: {
				tokens.next();
				String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
				IParsedType type = ParsedBasicType.UNDETERMINED;
				if (tokens.optional(ZSTokenType.K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedSetter(start, modifiers, annotations, name, type, body);
			}
			case K_GET: {
				tokens.next();
				String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
				IParsedType type = ParsedBasicType.UNDETERMINED;
				if (tokens.optional(ZSTokenType.K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				ParsedFunctionBody statements = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedGetter(start, modifiers, annotations, name, type, statements);
			}
			case K_IMPLEMENTS: {
				tokens.next();
				IParsedType type = IParsedType.parse(tokens);
				ParsedImplementation implementation = new ParsedImplementation(start, modifiers, annotations, type);
				if (tokens.optional(ZSTokenType.T_SEMICOLON) == null) {
					tokens.required(ZSTokenType.T_AOPEN, "{ expected");
					while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
						implementation.addMember(ParsedDefinitionMember.parse(tokens));
					}
				}
				return implementation;
			}
			case T_BROPEN: {
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedCaller(start, modifiers, annotations, header, body);
			}
			case T_SQOPEN: {
				tokens.next();
				tokens.required(ZSTokenType.T_SQCLOSE, "] expected");
				OperatorType operator = OperatorType.INDEXGET;
				if (tokens.optional(ZSTokenType.T_ASSIGN) != null) {
					operator = OperatorType.INDEXSET;
				}
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, modifiers, annotations, operator, header, body);
			}
			case T_CAT:
				tokens.pushMark();
				tokens.next();
				if (tokens.optional(ZSTokenType.K_THIS) != null) {
					tokens.popMark();

					// destructor
					ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
					return new ParsedDestructor(start, modifiers, annotations, body);
				}
				tokens.reset();
				// else it is a ~ operator, continue...
			case T_ADD:
			case T_SUB:
			case T_MUL:
			case T_DIV:
			case T_MOD:
			case T_AND:
			case T_OR:
			case T_XOR:
			case T_NOT:
			case T_ADDASSIGN:
			case T_SUBASSIGN:
			case T_CATASSIGN:
			case T_MULASSIGN:
			case T_DIVASSIGN:
			case T_MODASSIGN:
			case T_ANDASSIGN:
			case T_ORASSIGN:
			case T_XORASSIGN:
			case T_INCREMENT:
			case T_DECREMENT:
			case T_DOT2:
			case T_SHL:
			case T_SHR:
			case T_USHR:
			case T_SHLASSIGN:
			case T_SHRASSIGN:
			case T_USHRASSIGN: {
				ZSToken token = tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, modifiers, annotations, getOperator(token.type), header, body);
			}
			case T_EQUAL2: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, modifiers, annotations, OperatorType.EQUALS, header, body);
			}
			case K_AS: {
				tokens.next();
				IParsedType type = IParsedType.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedCaster(start, modifiers, annotations, type, body);
			}
			case K_IN: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, modifiers, annotations, OperatorType.CONTAINS, header, body);
			}
			case K_CLASS:
			case K_INTERFACE:
			case K_ALIAS:
			case K_STRUCT:
			case K_ENUM:
				return new ParsedInnerDefinition(ParsedDefinition.parse(start, modifiers, annotations, tokens));
			case K_FOR: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedIterator(start, modifiers, annotations, header, body);
			}
			default:
				if (modifiers == Modifiers.FLAG_STATIC && tokens.peek().type == ZSTokenType.T_AOPEN) {
					ParsedStatementBlock body = ParsedStatementBlock.parseBlock(tokens, annotations, true);
					return new ParsedStaticInitializer(tokens.getPosition(), annotations, body);
				}
				throw new ParseException(tokens.getPosition(), "Unexpected token: " + tokens.peek().content);
		}
	}

	private static OperatorType getOperator(ZSTokenType type) {
		switch (type) {
			case T_ADD:
				return OperatorType.ADD;
			case T_SUB:
				return OperatorType.SUB;
			case T_CAT:
				return OperatorType.CAT;
			case T_MUL:
				return OperatorType.MUL;
			case T_DIV:
				return OperatorType.DIV;
			case T_MOD:
				return OperatorType.MOD;
			case T_AND:
				return OperatorType.AND;
			case T_OR:
				return OperatorType.OR;
			case T_XOR:
				return OperatorType.XOR;
			case T_NOT:
				return OperatorType.NOT;
			case T_ADDASSIGN:
				return OperatorType.ADDASSIGN;
			case T_SUBASSIGN:
				return OperatorType.SUBASSIGN;
			case T_CATASSIGN:
				return OperatorType.CATASSIGN;
			case T_MULASSIGN:
				return OperatorType.MULASSIGN;
			case T_DIVASSIGN:
				return OperatorType.DIVASSIGN;
			case T_MODASSIGN:
				return OperatorType.MODASSIGN;
			case T_ANDASSIGN:
				return OperatorType.ANDASSIGN;
			case T_ORASSIGN:
				return OperatorType.ORASSIGN;
			case T_XORASSIGN:
				return OperatorType.XORASSIGN;
			case T_INCREMENT:
				return OperatorType.INCREMENT;
			case T_DECREMENT:
				return OperatorType.DECREMENT;
			case T_DOT2:
				return OperatorType.RANGE;
			case T_SHL:
				return OperatorType.SHL;
			case T_SHR:
				return OperatorType.SHR;
			case T_USHR:
				return OperatorType.USHR;
			case T_SHLASSIGN:
				return OperatorType.SHLASSIGN;
			case T_SHRASSIGN:
				return OperatorType.SHRASSIGN;
			case T_USHRASSIGN:
				return OperatorType.USHRASSIGN;
			default:
				throw new AssertionError("Missing switch case in getOperator");
		}
	}
}
