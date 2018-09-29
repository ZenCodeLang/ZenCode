/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedTypeParameter;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IParsedType {
	public static IParsedType parse(ZSTokenParser tokens) throws ParseException {
		IParsedType result = tryParse(tokens);
		if (result == null)
			throw new ParseException(tokens.getPosition(), "Type expected (got " + tokens.peek().content + ")");
		
		return result;
	}
	
	public static List<IParsedType> parseTypeArguments(ZSTokenParser tokens) throws ParseException {
		if (!tokens.isNext(T_LESS))
			return null;
		
		tokens.pushMark();
		tokens.next();
		
		List<IParsedType> genericParameters = new ArrayList<>();
		do {
			IParsedType type = tryParse(tokens);
			if (type == null) {
				tokens.reset();
				return Collections.emptyList();
			}
			genericParameters.add(type);
		} while (tokens.optional(T_COMMA) != null);
		
		if (tokens.isNext(T_SHR)) {
			tokens.replace(T_GREATER.flyweight);
		} else if (tokens.isNext(T_USHR)) {
			tokens.replace(T_SHR.flyweight);
		} else if (tokens.isNext(T_SHRASSIGN)) {
			tokens.replace(T_GREATEREQ.flyweight);
		} else if (tokens.isNext(T_USHRASSIGN)) {
			tokens.replace(T_SHRASSIGN.flyweight);
		} else if (tokens.optional(T_GREATER) == null) {
			tokens.reset();
			return Collections.emptyList();
		}
		
		tokens.popMark();
		return genericParameters;
	}
	
	public static List<IParsedType> parseTypeArgumentsForCall(ZSTokenParser tokens) throws ParseException {
		List<IParsedType> typeArguments = null;
		if (tokens.optional(ZSTokenType.T_LESS) != null) {
			try {
				typeArguments = new ArrayList<>();
				do {
					IParsedType type = IParsedType.parse(tokens);
					typeArguments.add(type);
				} while (tokens.optional(ZSTokenType.T_COMMA) != null);
				tokens.required(ZSTokenType.T_GREATER, "> expected");
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilTokenOrNewline(ZSTokenType.T_GREATER);
			}
		}
		return typeArguments;
	}
	
	public static IParsedType tryParse(ZSTokenParser tokens) throws ParseException {
		CodePosition position = tokens.getPosition();
		int modifiers = 0;
		while (true) {
			if (tokens.optional(ZSTokenType.K_CONST) != null) {
				modifiers |= ModifiedTypeID.MODIFIER_CONST;
			} else if (tokens.optional(ZSTokenType.K_IMMUTABLE) != null) {
				modifiers |= ModifiedTypeID.MODIFIER_IMMUTABLE;
			} else {
				break;
			}
		}
		
		IParsedType result;
		switch (tokens.peek().type) {
			case K_VOID:
				tokens.next();
				result = ParsedTypeBasic.VOID;
				break;
			case K_BOOL:
				tokens.next();
				result = ParsedTypeBasic.BOOL;
				break;
			case K_BYTE:
				tokens.next();
				result = ParsedTypeBasic.BYTE;
				break;
			case K_SBYTE:
				tokens.next();
				result = ParsedTypeBasic.SBYTE;
				break;
			case K_SHORT:
				tokens.next();
				result = ParsedTypeBasic.SHORT;
				break;
			case K_USHORT:
				tokens.next();
				result = ParsedTypeBasic.USHORT;
				break;
			case K_INT:
				tokens.next();
				result = ParsedTypeBasic.INT;
				break;
			case K_UINT:
				tokens.next();
				result = ParsedTypeBasic.UINT;
				break;
			case K_LONG:
				tokens.next();
				result = ParsedTypeBasic.LONG;
				break;
			case K_ULONG:
				tokens.next();
				result = ParsedTypeBasic.ULONG;
				break;
			case K_USIZE:
				tokens.next();
				result = ParsedTypeBasic.USIZE;
				break;
			case K_FLOAT:
				tokens.next();
				result = ParsedTypeBasic.FLOAT;
				break;
			case K_DOUBLE:
				tokens.next();
				result = ParsedTypeBasic.DOUBLE;
				break;
			case K_CHAR:
				tokens.next();
				result = ParsedTypeBasic.CHAR;
				break;
			case K_STRING: {
				tokens.next();
				ParsedStorageTag storage = ParsedStorageTag.parse(tokens);
				result = new ParsedString(position, modifiers, storage);
				break;
			}
			case K_FUNCTION: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				ParsedStorageTag storage = ParsedStorageTag.parse(tokens);
				result = new ParsedFunctionType(position, header, storage);
				break;
			}
			case T_IDENTIFIER: {
				List<ParsedNamedType.ParsedNamePart> name = new ArrayList<>();
				do {
					String namePart = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
					List<IParsedType> generic = parseTypeArguments(tokens);
					name.add(new ParsedNamedType.ParsedNamePart(namePart, generic));
				} while (tokens.optional(ZSTokenType.T_DOT) != null);
				ParsedStorageTag storage = ParsedStorageTag.parse(tokens);
				result = new ParsedNamedType(position, name, storage);
				break;
			}
			default:
				return null;
		}
		
		outer: while (true) {
			switch (tokens.peek().type) {
				case T_DOT2: {
					tokens.next();
					IParsedType to = parse(tokens);
					result = new ParsedTypeRange(position, result, to);
					break;
				}
				case T_SQOPEN:
					tokens.next();
					int dimension = 1;
					while (tokens.optional(ZSTokenType.T_COMMA) != null)
						dimension++;
					
					if (tokens.optional(ZSTokenType.T_SQCLOSE) != null) {
						ParsedStorageTag storage = ParsedStorageTag.parse(tokens);
						result = new ParsedTypeArray(position, result, dimension, storage);
					} else if (tokens.isNext(T_LESS)) {
						tokens.next();
						ParsedTypeParameter parameter = ParsedTypeParameter.parse(tokens);
						tokens.required(T_GREATER, "> expected");
						tokens.required(ZSTokenType.T_SQCLOSE, "] expected");
						ParsedStorageTag storage = ParsedStorageTag.parse(tokens);
						result = new ParsedTypeGenericMap(position, parameter, result, modifiers, storage);
					} else {
						IParsedType keyType = parse(tokens);
						tokens.required(ZSTokenType.T_SQCLOSE, "] expected");
						ParsedStorageTag storage = ParsedStorageTag.parse(tokens);
						result = new ParsedTypeAssociative(position, keyType, result, storage);
					}
					break;
				case T_QUEST:
					tokens.next();
					result = result.withOptional();
					break;
				default:
					break outer;
			}
		}
		
		if (modifiers > 0)
			result = result.withModifiers(modifiers);
		
		return result;
	}
	
	public static TypeID[] compileList(List<IParsedType> typeParameters, TypeResolutionContext context) {
		TypeID[] result = TypeID.NONE;
		if (typeParameters != null) {
			result = new TypeID[typeParameters.size()];
			for (int i = 0; i < typeParameters.size(); i++) {
				result[i] = typeParameters.get(i).compileUnstored(context);
			}
		}
		return result;
	}
	
	public static TypeID[] compileListUnstored(List<IParsedType> typeParameters, TypeResolutionContext context) {
		TypeID[] result = TypeID.NONE;
		if (typeParameters != null) {
			result = new TypeID[typeParameters.size()];
			for (int i = 0; i < typeParameters.size(); i++) {
				result[i] = typeParameters.get(i).compileUnstored(context);
			}
		}
		return result;
	}
	
	public IParsedType withOptional();
	
	public IParsedType withModifiers(int modifiers);
	
	public StoredType compile(TypeResolutionContext context);
	
	public TypeID compileUnstored(TypeResolutionContext context);
	
	public default AnnotationDefinition compileAnnotation(BaseScope scope) {
		return null;
	}
	
	public default TypeID[] compileTypeArguments(BaseScope scope) {
		return TypeID.NONE;
	}
}
