/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStruct extends BaseParsedDefinition {
	public static ParsedStruct parseStruct(ZSPackage pkg, CodePosition position, int modifiers, ZSTokenStream tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedGenericParameter> parameters = ParsedGenericParameter.parseAll(tokens);
		
		tokens.required(ZSTokenType.T_AOPEN, "{");
		
		ParsedStruct result = new ParsedStruct(pkg, position, modifiers, name, parameters, outerDefinition);
		while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
			result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled));
		}
		return result;
	}
	
	private final List<ParsedGenericParameter> parameters;
	
	private final StructDefinition compiled;
	
	public ParsedStruct(ZSPackage pkg, CodePosition position, int modifiers, String name, List<ParsedGenericParameter> genericParameters, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.parameters = genericParameters;
		
		compiled = new StructDefinition(position, pkg, name, modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedGenericParameter.getCompiled(genericParameters));
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void compileMembers(BaseScope scope) {
		ParsedGenericParameter.compile(scope, compiled.genericParameters, parameters);
		super.compileMembers(scope);
	}
}
