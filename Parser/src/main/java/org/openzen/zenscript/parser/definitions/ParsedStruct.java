/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStruct extends BaseParsedDefinition {
	public static ParsedStruct parseStruct(ZSPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);
		
		tokens.required(ZSTokenType.T_AOPEN, "{");
		
		ParsedStruct result = new ParsedStruct(pkg, position, modifiers, annotations, name, parameters, outerDefinition);
		while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
			result.addMember(ParsedDefinitionMember.parse(tokens, result.compiled, null));
		}
		return result;
	}
	
	private final List<ParsedTypeParameter> parameters;
	
	private final StructDefinition compiled;
	
	public ParsedStruct(ZSPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> genericParameters, HighLevelDefinition outerDefinition) {
		super(position, modifiers, annotations);
		
		this.parameters = genericParameters;
		
		compiled = new StructDefinition(position, pkg, name, modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(genericParameters));
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	protected void linkTypesLocal(TypeResolutionContext context) {
		ParsedTypeParameter.compile(context, compiled.genericParameters, parameters);
		super.linkTypesLocal(context);
	}
}
