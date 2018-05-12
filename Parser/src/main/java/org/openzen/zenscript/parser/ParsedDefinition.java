/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSTokenStream;
import static org.openzen.zenscript.lexer.ZSTokenType.K_ALIAS;
import static org.openzen.zenscript.lexer.ZSTokenType.K_CLASS;
import static org.openzen.zenscript.lexer.ZSTokenType.K_ENUM;
import static org.openzen.zenscript.lexer.ZSTokenType.K_EXPAND;
import static org.openzen.zenscript.lexer.ZSTokenType.K_FUNCTION;
import static org.openzen.zenscript.lexer.ZSTokenType.K_INTERFACE;
import static org.openzen.zenscript.lexer.ZSTokenType.K_STRUCT;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.definitions.ParsedAlias;
import org.openzen.zenscript.parser.definitions.ParsedClass;
import org.openzen.zenscript.parser.definitions.ParsedEnum;
import org.openzen.zenscript.parser.definitions.ParsedExpansion;
import org.openzen.zenscript.parser.definitions.ParsedFunction;
import org.openzen.zenscript.parser.definitions.ParsedInterface;
import org.openzen.zenscript.parser.definitions.ParsedStruct;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class ParsedDefinition {
	public static ParsedDefinition parse(ZSPackage pkg, CodePosition position, int modifiers, ZSTokenStream tokens, HighLevelDefinition outerDefinition) {
		if (tokens.optional(K_CLASS) != null) {
			return ParsedClass.parseClass(pkg, position, modifiers, tokens, outerDefinition);
		} else if (tokens.optional(K_INTERFACE) != null) {
			return ParsedInterface.parseInterface(pkg, position, modifiers, tokens, outerDefinition);
		} else if (tokens.optional(K_ENUM) != null) {
			return ParsedEnum.parseEnum(pkg, position, modifiers, tokens, outerDefinition);
		} else if (tokens.optional(K_STRUCT) != null) {
			return ParsedStruct.parseStruct(pkg, position, modifiers, tokens, outerDefinition);
		} else if (tokens.optional(K_ALIAS) != null) {
			return ParsedAlias.parseAlias(pkg, position, modifiers, tokens, outerDefinition);
		} else if (tokens.optional(K_FUNCTION) != null) {
			return ParsedFunction.parseFunction(pkg, position, modifiers, tokens, outerDefinition);
		} else if (tokens.optional(K_EXPAND) != null) {
			return ParsedExpansion.parseExpansion(pkg, position, modifiers, tokens, outerDefinition);
		} else {
			//tokens.required(EOF, "An import, class, interface, enum, struct, function or alias expected.");
			return null;
		}
	}
	
	public final CodePosition position;
	public final int modifiers;
	
	public ParsedDefinition(CodePosition position, int modifiers) {
		this.position = position;
		this.modifiers = modifiers;
	}
	
	public final CodePosition getPosition() {
		return position;
	}
	
	public final int getModifiers() {
		return modifiers;
	}
	
	public abstract HighLevelDefinition getCompiled();
	
	public abstract void linkInnerTypes();
	
	public void compileTypes(BaseScope scope) {
		
	}
	
	public abstract void compileMembers(BaseScope scope);
	
	public abstract void compileCode(BaseScope scope);
}
