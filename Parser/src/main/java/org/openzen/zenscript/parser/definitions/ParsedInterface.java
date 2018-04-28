/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedInterface extends BaseParsedDefinition {
	public static ParsedInterface parseInterface(ZSPackage pkg, CodePosition position, int modifiers, ZSTokenStream tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedGenericParameter> genericParameters = ParsedGenericParameter.parseAll(tokens);
		List<IParsedType> superInterfaces = Collections.emptyList();
		if (tokens.optional(ZSTokenType.T_COLON) != null) {
			superInterfaces = new ArrayList<>();
			do {
				superInterfaces.add(IParsedType.parse(tokens));
			} while (tokens.optional(ZSTokenType.T_COMMA) != null);
		}
		
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedInterface result = new ParsedInterface(pkg, position, modifiers, name, genericParameters, superInterfaces, outerDefinition);
		while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
			result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled));
		}
		return result;
	}
	
	private final List<ParsedGenericParameter> genericParameters;
	private final List<IParsedType> superInterfaces;
	
	private final InterfaceDefinition compiled;
	
	public ParsedInterface(ZSPackage pkg, CodePosition position, int modifiers, String name, List<ParsedGenericParameter> genericParameters, List<IParsedType> superInterfaces, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.genericParameters = genericParameters;
		this.superInterfaces = superInterfaces;
		
		compiled = new InterfaceDefinition(position, pkg, name, modifiers, outerDefinition);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void compileMembers(BaseScope scope) {
		for (IParsedType superInterface : superInterfaces)
			compiled.addBaseInterface(superInterface.compile(scope));
		
		super.compileMembers(scope);
	}
}
