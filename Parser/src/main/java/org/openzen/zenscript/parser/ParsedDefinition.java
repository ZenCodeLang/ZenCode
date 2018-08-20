/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.CompilingType;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.lexer.ZSTokenParser;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.definitions.ParsedAlias;
import org.openzen.zenscript.parser.definitions.ParsedClass;
import org.openzen.zenscript.parser.definitions.ParsedEnum;
import org.openzen.zenscript.parser.definitions.ParsedExpansion;
import org.openzen.zenscript.parser.definitions.ParsedFunction;
import org.openzen.zenscript.parser.definitions.ParsedInterface;
import org.openzen.zenscript.parser.definitions.ParsedStruct;
import org.openzen.zenscript.parser.definitions.ParsedVariant;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class ParsedDefinition {
	public static ParsedDefinition parse(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens, HighLevelDefinition outerDefinition) {
		if (tokens.optional(K_CLASS) != null) {
			return ParsedClass.parseClass(pkg, position, modifiers, annotations, tokens, outerDefinition);
		} else if (tokens.optional(K_INTERFACE) != null) {
			return ParsedInterface.parseInterface(pkg, position, modifiers, annotations, tokens, outerDefinition);
		} else if (tokens.optional(K_ENUM) != null) {
			return ParsedEnum.parseEnum(pkg, position, modifiers, annotations, tokens, outerDefinition);
		} else if (tokens.optional(K_STRUCT) != null) {
			return ParsedStruct.parseStruct(pkg, position, modifiers, annotations, tokens, outerDefinition);
		} else if (tokens.optional(K_ALIAS) != null) {
			return ParsedAlias.parseAlias(pkg, position, modifiers, annotations, tokens, outerDefinition);
		} else if (tokens.optional(K_FUNCTION) != null) {
			return ParsedFunction.parseFunction(pkg, position, modifiers, annotations, tokens, outerDefinition);
		} else if (tokens.optional(K_EXPAND) != null) {
			return ParsedExpansion.parseExpansion(pkg, position, modifiers, annotations, tokens, outerDefinition);
		} else if (tokens.optional(K_VARIANT) != null) {
			return ParsedVariant.parseVariant(pkg, position, modifiers, annotations, tokens, outerDefinition);
		} else {
			//tokens.required(EOF, "An import, class, interface, enum, struct, function or alias expected.");
			return null;
		}
	}
	
	public final CodePosition position;
	public final int modifiers;
	public final ParsedAnnotation[] annotations;
	public final CompilingPackage pkg;
	
	public ParsedDefinition(CodePosition position, int modifiers, CompilingPackage pkg, ParsedAnnotation[] annotations) {
		this.position = position;
		this.pkg = pkg;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}
	
	public String getName() {
		return getCompiled().name;
	}
	
	public final CodePosition getPosition() {
		return position;
	}
	
	public final int getModifiers() {
		return modifiers;
	}
	
	public abstract CompilingType getCompiling(TypeResolutionContext context);
	
	public abstract HighLevelDefinition getCompiled();
	
	public abstract void linkTypes(TypeResolutionContext context);
	
	public abstract void registerMembers(BaseScope scope, PrecompilationState state);
	
	public abstract void compile(BaseScope scope);
}
