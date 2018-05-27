/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ZSTokenParser;
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
	public static ParsedClass parseClass(ZSPackage pkg, CodePosition position, int modifiers, ZSTokenParser tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedGenericParameter> genericParameters = ParsedGenericParameter.parseAll(tokens);
		
		IParsedType superclass = null;
		if (tokens.optional(ZSTokenType.T_COLON) != null) {
			superclass = IParsedType.parse(tokens);
		}
		
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedClass result = new ParsedClass(pkg, position, modifiers, name, genericParameters, superclass, outerDefinition);
		while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
			result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled));
		}
		return result;
	}
	
	private final List<ParsedGenericParameter> genericParameters;
	private final IParsedType superclass;
	
	private final ClassDefinition compiled;
	
	public ParsedClass(ZSPackage pkg, CodePosition position, int modifiers, String name, List<ParsedGenericParameter> genericParameters, IParsedType superclass, HighLevelDefinition outerDefinition) {
		super(position, modifiers);
		
		this.genericParameters = genericParameters;
		this.superclass = superclass;
		
		compiled = new ClassDefinition(position, pkg, name, modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedGenericParameter.getCompiled(genericParameters));
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void compileMembers(BaseScope scope) {
		ParsedGenericParameter.compile(scope, compiled.genericParameters, genericParameters);
		if (superclass != null)
			compiled.setSuperclass(superclass.compile(new GenericFunctionScope(scope, compiled.genericParameters)));
		
		super.compileMembers(scope);
	}
}
