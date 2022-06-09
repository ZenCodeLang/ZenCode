package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.CompilableType;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.parser.definitions.*;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

public abstract class ParsedDefinition implements CompilableType {
	public final CodePosition position;
	public final int modifiers;
	public final ParsedAnnotation[] annotations;

	public ParsedDefinition(CodePosition position, int modifiers, ParsedAnnotation[] annotations) {
		this.position = position;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}

	public static ParsedDefinition parse(CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens) throws ParseException {
		if (tokens.optional(K_CLASS) != null) {
			return ParsedClass.parseClass(position, modifiers, annotations, tokens);
		} else if (tokens.optional(K_INTERFACE) != null) {
			return ParsedInterface.parseInterface(position, modifiers, annotations, tokens);
		} else if (tokens.optional(K_ENUM) != null) {
			return ParsedEnum.parseEnum(position, modifiers, annotations, tokens);
		} else if (tokens.optional(K_STRUCT) != null) {
			return ParsedStruct.parseStruct(position, modifiers, annotations, tokens);
		} else if (tokens.optional(K_ALIAS) != null) {
			return ParsedAlias.parseAlias(position, modifiers, annotations, tokens);
		} else if (tokens.optional(K_FUNCTION) != null) {
			return ParsedFunction.parseFunction(position, modifiers, annotations, tokens);
		} else if (tokens.optional(K_EXPAND) != null) {
			return ParsedExpansion.parseExpansion(position, modifiers, annotations, tokens);
		} else if (tokens.optional(K_VARIANT) != null) {
			return ParsedVariant.parseVariant(position, modifiers, annotations, tokens);
		} else {
			return null;
		}
	}

	public final CodePosition getPosition() {
		return position;
	}

	public final int getModifiers() {
		return modifiers;
	}
}
