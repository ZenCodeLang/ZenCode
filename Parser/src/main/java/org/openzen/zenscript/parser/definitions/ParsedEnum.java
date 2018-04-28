/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedEnum extends BaseParsedDefinition {
	public static ParsedEnum parseEnum(ZSPackage pkg, CodePosition position, int modifiers, ZSTokenStream tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		List<ParsedEnumConstant> enumValues = new ArrayList<>();
		while (!tokens.isNext(ZSTokenType.T_ACLOSE) && !tokens.isNext(ZSTokenType.T_SEMICOLON)) {
			enumValues.add(ParsedEnumConstant.parse(tokens));
			if (tokens.optional(ZSTokenType.T_COMMA) == null)
				break;
		}
		
		ParsedEnum result = new ParsedEnum(pkg, position, modifiers, name, enumValues, outerDefinition);
		if (tokens.optional(ZSTokenType.T_SEMICOLON) != null) {
			while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
				result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled));
			}
		} else {
			tokens.required(ZSTokenType.T_ACLOSE, "} expected");
		}
		return result;
	}
	
	private final List<ParsedEnumConstant> enumValues;
	
	private final EnumDefinition compiled;
	
	public ParsedEnum(ZSPackage pkg, CodePosition position, int modifiers, String name, List<ParsedEnumConstant> enumValues, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.enumValues = enumValues;
		
		compiled = new EnumDefinition(pkg, name, modifiers, outerDefinition);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}
}
