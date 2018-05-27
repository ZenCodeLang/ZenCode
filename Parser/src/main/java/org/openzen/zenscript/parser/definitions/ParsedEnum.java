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
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedEnum extends BaseParsedDefinition {
	public static ParsedEnum parseEnum(ZSPackage pkg, CodePosition position, int modifiers, ZSTokenParser tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedEnum result = new ParsedEnum(pkg, position, modifiers, name, outerDefinition);
		
		while (!tokens.isNext(ZSTokenType.T_ACLOSE) && !tokens.isNext(ZSTokenType.T_SEMICOLON)) {
			result.addEnumValue(ParsedEnumConstant.parse(tokens, result.compiled, result.enumValues.size()));
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
	
	private final List<ParsedEnumConstant> enumValues = new ArrayList<>();
	
	private final EnumDefinition compiled;
	
	public ParsedEnum(ZSPackage pkg, CodePosition position, int modifiers, String name, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		compiled = new EnumDefinition(position, pkg, name, modifiers, outerDefinition);
	}
	
	public void addEnumValue(ParsedEnumConstant value) {
		enumValues.add(value);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}
	
	@Override
	public void compileMembers(BaseScope scope) {
		super.compileMembers(scope);
		
		for (ParsedEnumConstant constant : enumValues) {
			compiled.addEnumConstant(constant.getCompiled());
		}
	}

	@Override
	public void compileCode(BaseScope scope) {
		super.compileCode(scope);
		
		DefinitionTypeID type = new DefinitionTypeID(compiled, new ITypeID[0]);
		ExpressionScope evalScope = new ExpressionScope(scope);
		for (ParsedEnumConstant constant : enumValues) {
			constant.compileCode(type, evalScope);
		}
	}
}
