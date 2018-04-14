/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.DefinitionScope;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedAlias extends ParsedDefinition {
	public static ParsedAlias parseAlias(CodePosition position, int modifiers, ZSTokenStream tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedGenericParameter> parameters = ParsedGenericParameter.parseAll(tokens);
		IParsedType type = IParsedType.parse(tokens);
		return new ParsedAlias(position, modifiers, name, parameters, type, outerDefinition);
	}
	
	private final String name;
	private final List<ParsedGenericParameter> parameters;
	private final IParsedType type;
	
	private final AliasDefinition compiled;
	
	public ParsedAlias(CodePosition position, int modifiers, String name, List<ParsedGenericParameter> parameters, IParsedType type, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.name = name;
		this.parameters = parameters;
		this.type = type;
		
		compiled = new AliasDefinition(name, modifiers, outerDefinition);
	}
	
	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void compileMembers(BaseScope scope) {
		for (ParsedGenericParameter parameter : this.parameters) {
			compiled.addGenericParameter(parameter.compiled);
		}
		
		DefinitionScope innerScope = new DefinitionScope(scope, compiled);
		for (int i = 0; i < compiled.genericParameters.size(); i++) {
			TypeParameter output = compiled.genericParameters.get(i);
			ParsedGenericParameter input = this.parameters.get(i);
			for (ParsedGenericBound bound : input.bounds) {
				output.addBound(bound.compile(innerScope));
			}
		}
		
		compiled.setType(type.compile(innerScope));
	}

	@Override
	public void compileCode(BaseScope scope) {
		// nothing to do
	}

	@Override
	public void linkInnerTypes() {
		// nothing to do
	}
}
