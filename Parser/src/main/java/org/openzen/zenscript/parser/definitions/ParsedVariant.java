/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.DefinitionScope;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedVariant extends BaseParsedDefinition {
	public static ParsedVariant parseVariant(ZSPackage pkg, CodePosition position, int modifiers, ZSTokenParser tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedGenericParameter> typeParameters = ParsedGenericParameter.parseAll(tokens);
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedVariant result = new ParsedVariant(pkg, position, modifiers, name, typeParameters, outerDefinition);
		
		while (!tokens.isNext(ZSTokenType.T_ACLOSE) && !tokens.isNext(ZSTokenType.T_SEMICOLON)) {
			String optionName = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
			List<IParsedType> types = new ArrayList<>();
			if (tokens.optional(ZSTokenType.T_BROPEN) != null) {
				types.add(IParsedType.parse(tokens));
				while (tokens.optional(ZSTokenType.T_COMMA) != null) {
					types.add(IParsedType.parse(tokens));
				}
				tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
			}
			result.addVariant(new ParsedVariantOption(optionName, types));
			if (tokens.optional(ZSTokenType.T_COMMA) == null)
				break;
		}
		
		if (tokens.optional(ZSTokenType.T_SEMICOLON) != null) {
			while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
				result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled));
			}
		} else {
			tokens.required(ZSTokenType.T_ACLOSE, "} expected");
		}
		return result;
	}
	
	private final List<ParsedGenericParameter> typeParameters;
	private final List<ParsedVariantOption> variants = new ArrayList<>();
	
	private final VariantDefinition compiled;
	
	public ParsedVariant(ZSPackage pkg, CodePosition position, int modifiers, String name, List<ParsedGenericParameter> typeParameters, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.typeParameters = typeParameters;
		compiled = new VariantDefinition(position, pkg, name, modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedGenericParameter.getCompiled(typeParameters));
	}
	
	public void addVariant(ParsedVariantOption value) {
		variants.add(value);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}
	
	@Override
	public void compileTypes(BaseScope scope) {
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled(), false);
		for (ParsedVariantOption variant : variants) {
			compiled.options.add(variant.compile(innerScope));
		}
	}
	
	@Override
	public void compileMembers(BaseScope scope) {
		ParsedGenericParameter.compile(scope, compiled.genericParameters, typeParameters);
		super.compileMembers(scope);
	}
}
