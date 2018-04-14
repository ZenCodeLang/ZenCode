/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.GenericFunctionScope;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpansion extends BaseParsedDefinition {
	public static ParsedExpansion parseExpansion(CodePosition position, int modifiers, ZSTokenStream tokens, HighLevelDefinition outerDefinition) {
		List<ParsedGenericParameter> parameters = ParsedGenericParameter.parseAll(tokens);
		IParsedType target = IParsedType.parse(tokens);
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedExpansion result = new ParsedExpansion(position, modifiers, parameters, target, outerDefinition);
		while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
			result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled));
		}
		return result;
	}
	
	private final List<ParsedGenericParameter> parameters;
	private final IParsedType target;
	private final ExpansionDefinition compiled;
	
	public ParsedExpansion(CodePosition position, int modifiers, List<ParsedGenericParameter> parameters, IParsedType target, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.parameters = parameters;
		this.target = target;
		
		this.compiled = new ExpansionDefinition(modifiers, outerDefinition);
		for (ParsedGenericParameter parameter : parameters)
			compiled.addGenericParameter(parameter.compiled);
	}
	
	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void compileMembers(BaseScope scope) {
		TypeParameter[] parameters = ParsedGenericParameter.compile(scope, this.parameters);
		compiled.target = target.compile(new GenericFunctionScope(scope, parameters));
		
		super.compileMembers(scope);
	}
}
