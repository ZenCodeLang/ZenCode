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
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedStatement;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class ParsedDefinitionMember {
	public static ParsedDefinitionMember parse(ZSTokenStream tokens, HighLevelDefinition forDefinition) {
		CodePosition start = tokens.getPosition();
		int modifiers = 0;
		while (true) {
			if (tokens.optional(ZSTokenType.K_EXPORT) != null) {
				modifiers |= Modifiers.MODIFIER_EXPORT;
			} else if (tokens.optional(ZSTokenType.K_PUBLIC) != null) {
				modifiers |= Modifiers.MODIFIER_PUBLIC;
			} else if (tokens.optional(ZSTokenType.K_PRIVATE) != null) {
				modifiers |= Modifiers.MODIFIER_PRIVATE;
			} else if (tokens.optional(ZSTokenType.K_CONST) != null) {
				if (tokens.optional(ZSTokenType.T_QUEST) != null) {
					modifiers |= Modifiers.MODIFIER_CONST_OPTIONAL;
				} else {
					modifiers |= Modifiers.MODIFIER_CONST;
				}
			} else if (tokens.optional(ZSTokenType.K_ABSTRACT) != null) {
				modifiers |= Modifiers.MODIFIER_ABSTRACT;
			} else if (tokens.optional(ZSTokenType.K_FINAL) != null) {
				modifiers |= Modifiers.MODIFIER_FINAL;
			} else if (tokens.optional(ZSTokenType.K_STATIC) != null) {
				modifiers |= Modifiers.MODIFIER_STATIC;
			} else if (tokens.optional(ZSTokenType.K_PROTECTED) != null) {
				modifiers |= Modifiers.MODIFIER_PROTECTED;
			} else if (tokens.optional(ZSTokenType.K_IMPLICIT) != null) {
				modifiers |= Modifiers.MODIFIER_IMPLICIT;
			} else {
				break;
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
				if (tokens.optional(ZSTokenType.T_ASSIGN) != null) {
					initializer = ParsedExpression.parse(tokens);
				}
				tokens.required(ZSTokenType.T_SEMICOLON, "; expected");
				return new ParsedField(start, modifiers, name, type, initializer, t.type == ZSTokenType.K_VAL);
			}
			case K_THIS: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				if (body == null)
					throw new CompileException(start, CompileExceptionCode.METHOD_BODY_REQUIRED, "Function body is required for constructors");
				
				return new ParsedConstructor(start, modifiers, header, body);
			}
			case T_IDENTIFIER: {
				String name = tokens.next().content;
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedMethod(start, modifiers, name, header, body);
			}
			case K_SET: {
				tokens.next();
				String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
				IParsedType type = ParsedTypeBasic.ANY;
				if (tokens.optional(ZSTokenType.K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedSetter(start, modifiers, name, type, body);
			}
			case K_GET: {
				tokens.next();
				String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
				IParsedType type = ParsedTypeBasic.ANY;
				if (tokens.optional(ZSTokenType.K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				ParsedFunctionBody statements = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedGetter(start, modifiers, name, type, statements);
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
				return new ParsedImplementation(start, modifiers, type, members);
			}
			case T_BROPEN: {
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedCaller(start, modifiers, header, body);
			}
			case T_SQOPEN: {
				tokens.required(ZSTokenType.T_SQCLOSE, "] expected");
				OperatorType operator = OperatorType.INDEXGET;
				if (tokens.optional(ZSTokenType.T_ASSIGN) != null) {
					operator = OperatorType.INDEXSET;
				}
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, modifiers, operator, header, body);
			}
			case T_ADD:
			case T_SUB:
			case T_CAT:
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
			case T_DECREMENT: {
				ZSToken token = tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, modifiers, getOperator(token.type), header, body);
			}
			case T_EQUAL2: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, modifiers, OperatorType.EQUALS, header, body);
			}
			case K_AS: {
				tokens.next();
				IParsedType type = IParsedType.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedCaster(start, modifiers, type, body);
			}
			case K_IN: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedOperator(start, modifiers, OperatorType.CONTAINS, header, body);
			}
			case K_CLASS:
			case K_INTERFACE:
			case K_ALIAS:
			case K_STRUCT:
			case K_ENUM:
				return new ParsedInnerDefinition(ParsedDefinition.parse(start, modifiers, tokens, forDefinition));
			case K_FOR: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedFunctionBody body = ParsedStatement.parseFunctionBody(tokens);
				return new ParsedIterator(start, modifiers, header, body);
			}
			default:
				throw new CompileException(tokens.peek().position, CompileExceptionCode.UNEXPECTED_TOKEN, "Unexpected token: " + tokens.peek().content);
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
			case T_INCREMENT: return OperatorType.POST_INCREMENT;
			case T_DECREMENT: return OperatorType.POST_DECREMENT;
			default:
				throw new AssertionError("Missing switch case in getOperator");
		}
	}
	
	public abstract void linkInnerTypes(HighLevelDefinition definition);
	
	public abstract void linkTypes(BaseScope scope);
	
	public abstract IDefinitionMember getCompiled();
	
	public abstract void compile(BaseScope scope);
}
