/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.parser.statements.ParsedStatementBlock;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class ParsedDefinitionMember {
	public static ParsedDefinitionMember parse(ZSTokenParser tokens, HighLevelDefinition forDefinition) {
		CodePosition start = tokens.getPosition();
		ParsedAnnotation[] annotations = ParsedAnnotation.parseAnnotations(tokens);
		int modifiers = 0;
		outer: while (true) {
			switch (tokens.peek().type) {
				case K_EXPORT:
					tokens.next();
					modifiers |= Modifiers.EXPORT;
					break;
				case K_PUBLIC:
					tokens.next();
					modifiers |= Modifiers.PUBLIC;
					break;
				case K_PRIVATE:
					tokens.next();
					modifiers |= Modifiers.PRIVATE;
					break;
				case K_CONST:
					tokens.next();
					if (tokens.optional(ZSTokenType.T_QUEST) != null) {
						modifiers |= Modifiers.CONST_OPTIONAL;
					} else {
						modifiers |= Modifiers.CONST;
					}
					break;
				case K_ABSTRACT:
					tokens.next();
					modifiers |= Modifiers.ABSTRACT;
					break;
				case K_FINAL:
					tokens.next();
					modifiers |= Modifiers.FINAL;
					break;
				case K_STATIC:
					tokens.next();
					modifiers |= Modifiers.STATIC;
					break;
				case K_PROTECTED:
					tokens.next();
					modifiers |= Modifiers.PROTECTED;
					break;
				case K_IMPLICIT:
					tokens.next();
					modifiers |= Modifiers.IMPLICIT;
					break;
				case K_EXTERN:
					tokens.next();
					modifiers |= Modifiers.EXTERN;
					break;
				case K_OVERRIDE:
					tokens.next();
					modifiers |= Modifiers.OVERRIDE;
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
				IParsedType type = ParsedTypeBasic.ANY;
				if (tokens.optional(ZSTokenType.K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				ParsedExpression initializer = null;
				int autoGetter = 0;
				int autoSetter = 0;
				if (tokens.optional(ZSTokenType.T_COLON) != null) {
					do {
						int accessor = Modifiers.PUBLIC;
						if (tokens.optional(ZSTokenType.K_PUBLIC) != null) {
							accessor = Modifiers.PUBLIC;
						} else if (tokens.optional(ZSTokenType.K_PROTECTED) != null) {
							accessor = Modifiers.PROTECTED;
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
				return new ParsedField(start, forDefinition, modifiers, annotations, name, type, initializer, t.type == ZSTokenType.K_VAL, autoGetter, autoSetter);
			}
			case K_THIS: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				if (body == null)
					throw new CompileException(start, CompileExceptionCode.METHOD_BODY_REQUIRED, "Function body is required for constructors");
				
				return new ParsedConstructor(start, forDefinition, modifiers, annotations, header, body);
			}
			case T_IDENTIFIER: {
				String name = tokens.next().content;
				if ((modifiers & Modifiers.CONST) == Modifiers.CONST && tokens.isNext(ZSTokenType.K_AS)) {
					tokens.next();
					IParsedType type = IParsedType.parse(tokens);
					tokens.required(ZSTokenType.T_ASSIGN, "= expected");
					ParsedExpression value = ParsedExpression.parse(tokens);
					tokens.required(ZSTokenType.T_SEMICOLON, "; expected");
					return new ParsedConst(start, forDefinition, modifiers & ~Modifiers.CONST, annotations, name, type, value);
				}
				
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedMethod(start, forDefinition, modifiers, annotations, name, header, body);
			}
			case K_SET: {
				tokens.next();
				String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
				IParsedType type = ParsedTypeBasic.ANY;
				if (tokens.optional(ZSTokenType.K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedSetter(start, forDefinition, modifiers, annotations, name, type, body);
			}
			case K_GET: {
				tokens.next();
				String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
				IParsedType type = ParsedTypeBasic.ANY;
				if (tokens.optional(ZSTokenType.K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				ParsedFunctionBody statements = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedGetter(start, forDefinition, modifiers, annotations, name, type, statements);
			}
			case K_IMPLEMENTS: {
				tokens.next();
				IParsedType type = IParsedType.parse(tokens);
				List<ParsedDefinitionMember> members = new ArrayList<>();
				if (tokens.optional(ZSTokenType.T_SEMICOLON) == null) {
					tokens.required(ZSTokenType.T_AOPEN, "{ expected");
					while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
						members.add(ParsedDefinitionMember.parse(tokens, forDefinition));
					}
				}
				return new ParsedImplementation(start, forDefinition, modifiers, annotations, type, members);
			}
			case T_BROPEN: {
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedCaller(start, forDefinition, modifiers, annotations, header, body);
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
				return new ParsedOperator(start, forDefinition, modifiers, annotations, operator, header, body);
			}
			case T_CAT:
				tokens.pushMark();
				tokens.next();
				if (tokens.optional(ZSTokenType.K_THIS) != null) {
					tokens.popMark();
					
					// destructor
					ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
					return new ParsedDestructor(start, forDefinition, modifiers, annotations, body);
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
				return new ParsedOperator(start, forDefinition, modifiers, annotations, getOperator(token.type), header, body);
			}
			case T_EQUAL2: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, forDefinition, modifiers, annotations, OperatorType.EQUALS, header, body);
			}
			case K_AS: {
				tokens.next();
				IParsedType type = IParsedType.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedCaster(start, forDefinition, modifiers, annotations, type, body);
			}
			case K_IN: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, forDefinition, modifiers, annotations, OperatorType.CONTAINS, header, body);
			}
			case K_CLASS:
			case K_INTERFACE:
			case K_ALIAS:
			case K_STRUCT:
			case K_ENUM:
				return new ParsedInnerDefinition(forDefinition, ParsedDefinition.parse(forDefinition.pkg, start, modifiers, annotations, tokens, forDefinition));
			case K_FOR: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedIterator(start, forDefinition, modifiers, annotations, header, body);
			}
			default:
				if (modifiers == Modifiers.STATIC && tokens.peek().type == ZSTokenType.T_AOPEN) {
					ParsedStatementBlock body = ParsedStatementBlock.parseBlock(tokens, annotations, true);
					return new ParsedStaticInitializer(forDefinition, tokens.getPosition(), annotations, body);
				}
				throw new CompileException(tokens.getPosition(), CompileExceptionCode.UNEXPECTED_TOKEN, "Unexpected token: " + tokens.peek().content);
		}
	}
	
	private static OperatorType getOperator(ZSTokenType type) {
		switch (type) {
			case T_ADD: return OperatorType.ADD;
			case T_SUB: return OperatorType.SUB;
			case T_CAT: return OperatorType.CAT;
			case T_MUL: return OperatorType.MUL;
			case T_DIV: return OperatorType.DIV;
			case T_MOD: return OperatorType.MOD;
			case T_AND: return OperatorType.AND;
			case T_OR: return OperatorType.OR;
			case T_XOR: return OperatorType.XOR;
			case T_NOT: return OperatorType.NOT;
			case T_ADDASSIGN: return OperatorType.ADDASSIGN;
			case T_SUBASSIGN: return OperatorType.SUBASSIGN;
			case T_CATASSIGN: return OperatorType.CATASSIGN;
			case T_MULASSIGN: return OperatorType.MULASSIGN;
			case T_DIVASSIGN: return OperatorType.DIVASSIGN;
			case T_MODASSIGN: return OperatorType.MODASSIGN;
			case T_ANDASSIGN: return OperatorType.ANDASSIGN;
			case T_ORASSIGN: return OperatorType.ORASSIGN;
			case T_XORASSIGN: return OperatorType.XORASSIGN;
			case T_INCREMENT: return OperatorType.INCREMENT;
			case T_DECREMENT: return OperatorType.DECREMENT;
			case T_DOT2: return OperatorType.RANGE;
			case T_SHL: return OperatorType.SHL;
			case T_SHR: return OperatorType.SHR;
			case T_USHR: return OperatorType.USHR;
			case T_SHLASSIGN: return OperatorType.SHLASSIGN;
			case T_SHRASSIGN: return OperatorType.SHRASSIGN;
			case T_USHRASSIGN: return OperatorType.USHRASSIGN;
			default:
				throw new AssertionError("Missing switch case in getOperator");
		}
	}
	
	public final HighLevelDefinition definition;
	public final ParsedAnnotation[] annotations;
	
	public ParsedDefinitionMember(HighLevelDefinition definition, ParsedAnnotation[] annotations) {
		this.definition = definition;
		this.annotations = annotations;
	}
	
	public abstract void linkInnerTypes();
	
	public abstract void linkTypes(BaseScope scope);
	
	public abstract IDefinitionMember getCompiled();
	
	public abstract void compile(BaseScope scope);
}
