/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedStruct extends BaseParsedDefinition {
	public static ParsedStruct parseStruct(
			CompilingPackage pkg,
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens,
			HighLevelDefinition outerDefinition) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);
		
		tokens.required(ZSTokenType.T_AOPEN, "{");
		
		ParsedStruct result = new ParsedStruct(pkg, position, modifiers, annotations, name, parameters, outerDefinition);
		try {
			while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
				result.addMember(ParsedDefinitionMember.parse(tokens, result, null));
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
			tokens.recoverUntilOnToken(ZSTokenType.T_ACLOSE);
		}
		return result;
	}
	
	private final List<ParsedTypeParameter> parameters;
	
	private final StructDefinition compiled;
	
	public ParsedStruct(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> genericParameters, HighLevelDefinition outerDefinition) {
		super(position, modifiers, pkg, annotations);
		
		this.parameters = genericParameters;
		
		compiled = new StructDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(genericParameters));
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	protected void linkTypesLocal(TypeResolutionContext context) {
		ParsedTypeParameter.compile(context, compiled.typeParameters, parameters);
		super.linkTypesLocal(context);
	}
}
