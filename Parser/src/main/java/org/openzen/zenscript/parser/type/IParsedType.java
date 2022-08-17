package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedTypeParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

public interface IParsedType {
	static IParsedType parse(ZSTokenParser tokens) throws ParseException {
		IParsedType result = tryParse(tokens);
		if (result == null)
			throw new ParseException(tokens.getPosition(), "Type expected (got " + tokens.peek().content + ")");

		return result;
	}

	static List<IParsedType> parseTypeArguments(ZSTokenParser tokens) throws ParseException {
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

	static List<IParsedType> parseTypeArgumentsForCall(ZSTokenParser tokens) throws ParseException {
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

	static IParsedType tryParse(ZSTokenParser tokens) throws ParseException {
		CodePosition position = tokens.getPosition();

		IParsedType result;
		switch (tokens.peek().type) {
			case K_VOID:
				tokens.next();
				result = ParsedBasicType.VOID;
				break;
			case K_BOOL:
				tokens.next();
				result = ParsedBasicType.BOOL;
				break;
			case K_BYTE:
				tokens.next();
				result = ParsedBasicType.BYTE;
				break;
			case K_SBYTE:
				tokens.next();
				result = ParsedBasicType.SBYTE;
				break;
			case K_SHORT:
				tokens.next();
				result = ParsedBasicType.SHORT;
				break;
			case K_USHORT:
				tokens.next();
				result = ParsedBasicType.USHORT;
				break;
			case K_INT:
				tokens.next();
				result = ParsedBasicType.INT;
				break;
			case K_UINT:
				tokens.next();
				result = ParsedBasicType.UINT;
				break;
			case K_LONG:
				tokens.next();
				result = ParsedBasicType.LONG;
				break;
			case K_ULONG:
				tokens.next();
				result = ParsedBasicType.ULONG;
				break;
			case K_USIZE:
				tokens.next();
				result = ParsedBasicType.USIZE;
				break;
			case K_FLOAT:
				tokens.next();
				result = ParsedBasicType.FLOAT;
				break;
			case K_DOUBLE:
				tokens.next();
				result = ParsedBasicType.DOUBLE;
				break;
			case K_CHAR:
				tokens.next();
				result = ParsedBasicType.CHAR;
				break;
			case K_STRING: {
				tokens.next();
				result = ParsedBasicType.STRING;
				break;
			}
			case K_FUNCTION: {
				tokens.next();
				ParsedFunctionHeader header = ParsedFunctionHeader.parse(tokens);
				result = new ParsedFunctionType(header);
				break;
			}
			case T_IDENTIFIER: {
				List<ParsedNamedType.ParsedNamePart> name = new ArrayList<>();
				do {
					String namePart = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
					List<IParsedType> generic = parseTypeArguments(tokens);
					name.add(new ParsedNamedType.ParsedNamePart(namePart, generic));
				} while (tokens.optional(ZSTokenType.T_DOT) != null);
				result = new ParsedNamedType(position, name);
				break;
			}
			default:
				return null;
		}

		outer:
		while (true) {
			switch (tokens.peek().type) {
				case T_DOT2: {
					tokens.next();
					IParsedType to = parse(tokens);
					result = new ParsedRangeType(position, result, to);
					break;
				}
				case T_SQOPEN:
					tokens.next();
					int dimension = 1;
					while (tokens.optional(ZSTokenType.T_COMMA) != null)
						dimension++;

					if (tokens.optional(ZSTokenType.T_SQCLOSE) != null) {
						result = new ParsedArrayType(result, dimension);
					} else if (tokens.isNext(T_LESS)) {
						tokens.next();
						ParsedTypeParameter parameter = ParsedTypeParameter.parse(tokens);
						tokens.required(T_GREATER, "> expected");
						tokens.required(ZSTokenType.T_SQCLOSE, "] expected");
						result = new ParsedGenericMapType(parameter, result);
					} else {
						IParsedType keyType = parse(tokens);
						tokens.required(ZSTokenType.T_SQCLOSE, "] expected");
						result = new ParsedMapType(keyType, result);
					}
					break;
				case T_QUEST:
					tokens.next();
					result = new ParsedOptionalType(result);
					break;
				default:
					break outer;
			}
		}

		return result;
	}

	static TypeID[] compileTypes(List<IParsedType> typeArguments, TypeBuilder typeBuilder) {
		TypeID[] result = TypeID.NONE;
		if (typeArguments != null && typeArguments.size() > 0) {
			result = new TypeID[typeArguments.size()];
			for (int i = 0; i < typeArguments.size(); i++) {
				result[i] = typeArguments.get(i).compile(typeBuilder);
			}
		}
		return result;
	}

	TypeID compile(TypeBuilder typeBuilder);

	default Optional<AnnotationDefinition> compileAnnotation(TypeBuilder typeBuilder) {
		return Optional.empty();
	}

	default TypeID[] compileTypeArguments(TypeBuilder typeBuilder) {
		return TypeID.NONE;
	}
}
