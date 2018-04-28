/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
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
 * @author Stan Hebben
 */
public class ParsedClass extends BaseParsedDefinition {
	public static ParsedClass parseClass(CodePosition position, int modifiers, ZSTokenStream tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedGenericParameter> genericParameters = ParsedGenericParameter.parseAll(tokens);
		
		IParsedType superclass = null;
		if (tokens.optional(ZSTokenType.T_COLON) != null) {
			superclass = IParsedType.parse(tokens);
		}
		
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedClass result = new ParsedClass(position, modifiers, name, genericParameters, superclass, outerDefinition);
		while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
			result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled));
		}
		return result;
	}
	
	private final List<ParsedGenericParameter> genericParameters;
	private final IParsedType superclass;
	
	private final ClassDefinition compiled;
	
	public ParsedClass(CodePosition position, int modifiers, String name, List<ParsedGenericParameter> genericParameters, IParsedType superclass, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.genericParameters = genericParameters;
		this.superclass = superclass;
		
		compiled = new ClassDefinition(name, modifiers, outerDefinition);
		for (ParsedGenericParameter parameter : genericParameters)
			compiled.addGenericParameter(parameter.compiled);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void compileMembers(BaseScope scope) {
		TypeParameter[] parameters = ParsedGenericParameter.compile(scope, genericParameters);
		if (superclass != null)
			compiled.setSuperclass(superclass.compile(new GenericFunctionScope(scope, parameters)));
		
		super.compileMembers(scope);
	}
}
