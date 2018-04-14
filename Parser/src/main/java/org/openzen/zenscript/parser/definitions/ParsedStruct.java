/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
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
	public static ParsedStruct parseStruct(CodePosition position, int modifiers, ZSTokenStream tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedGenericParameter> parameters = ParsedGenericParameter.parseAll(tokens);
		
		tokens.required(ZSTokenType.T_AOPEN, "{");
		
		ParsedStruct result = new ParsedStruct(position, modifiers, name, parameters, outerDefinition);
		while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
			result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled));
		}
		return result;
	}
	
	private final String name;
	private final List<ParsedGenericParameter> parameters;
	
	private final StructDefinition compiled;
	
	public ParsedStruct(CodePosition position, int modifiers, String name, List<ParsedGenericParameter> parameters, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.name = name;
		this.parameters = parameters;
		
		compiled = new StructDefinition(name, modifiers, outerDefinition);
		for (ParsedGenericParameter parameter : parameters)
			compiled.addGenericParameter(parameter.compiled);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void compileMembers(BaseScope scope) {
		ParsedGenericParameter.compile(scope, parameters);
		
		super.compileMembers(scope);
	}
}
