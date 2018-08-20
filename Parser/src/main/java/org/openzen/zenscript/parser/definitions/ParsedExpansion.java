/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpansion extends BaseParsedDefinition {
	public static ParsedExpansion parseExpansion(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens, HighLevelDefinition outerDefinition) {
		List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);
		IParsedType target = IParsedType.parse(tokens);
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedExpansion result = new ParsedExpansion(pkg, position, modifiers, annotations, parameters, target, outerDefinition);
		while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
			result.addMember(ParsedDefinitionMember.parse(tokens, result, null));
		}
		return result;
	}
	
	private final List<ParsedTypeParameter> parameters;
	private final IParsedType target;
	private final ExpansionDefinition compiled;
	
	public ParsedExpansion(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, List<ParsedTypeParameter> genericParameters, IParsedType target, HighLevelDefinition outerDefinition) {
		super(position, modifiers, pkg, annotations);
		
		this.parameters = genericParameters;
		this.target = target;
		
		compiled = new ExpansionDefinition(position, pkg.getPackage(), modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(genericParameters));
	}
	
	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void linkTypesLocal(TypeResolutionContext context) {
		ParsedTypeParameter.compile(context, compiled.genericParameters, this.parameters);
		compiled.target = target.compile(context);
		if (compiled.target == null)
			throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Could not compile expansion target: " + target);
		
		super.linkTypesLocal(context);
	}
}
