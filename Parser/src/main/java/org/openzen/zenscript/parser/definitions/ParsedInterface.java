/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedInterface extends BaseParsedDefinition {
	public static ParsedInterface parseInterface(
			CompilingPackage pkg,
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens,
			HighLevelDefinition outerDefinition) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> genericParameters = ParsedTypeParameter.parseAll(tokens);
		List<IParsedType> superInterfaces = Collections.emptyList();
		try {
			if (tokens.optional(ZSTokenType.T_COLON) != null) {
				superInterfaces = new ArrayList<>();
				do {
					superInterfaces.add(IParsedType.parse(tokens));
				} while (tokens.optional(ZSTokenType.T_COMMA) != null);
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
		}
		
		ParsedInterface result = new ParsedInterface(pkg, position, modifiers, annotations, name, genericParameters, superInterfaces, outerDefinition);
		
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		try {
			while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
				result.addMember(ParsedDefinitionMember.parse(tokens, result, null));
			}
		} catch (ParseException ex) {
			tokens.recoverUntilToken(ZSTokenType.T_ACLOSE);
		}
		return result;
	}
	
	private final List<ParsedTypeParameter> typeParameters;
	private final List<IParsedType> superInterfaces;
	
	private final InterfaceDefinition compiled;
	
	public ParsedInterface(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> typeParameters, List<IParsedType> superInterfaces, HighLevelDefinition outerDefinition) {
		super(position, modifiers, pkg, annotations);
		
		this.typeParameters = typeParameters;
		this.superInterfaces = superInterfaces;
		
		compiled = new InterfaceDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(typeParameters));
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}
	
	@Override
	public void linkTypesLocal(TypeResolutionContext context) {
		ParsedTypeParameter.compile(context, compiled.typeParameters, typeParameters);
		
		for (IParsedType superInterface : superInterfaces)
			compiled.addBaseInterface(superInterface.compile(context));
		
		super.linkTypesLocal(context);
	}
}
