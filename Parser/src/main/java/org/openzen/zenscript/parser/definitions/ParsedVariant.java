/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
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
public class ParsedVariant extends BaseParsedDefinition {
	public static ParsedVariant parseVariant(
			CompilingPackage pkg,
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser tokens,
			HighLevelDefinition outerDefinition) throws ParseException {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> typeParameters = ParsedTypeParameter.parseAll(tokens);
		tokens.required(ZSTokenType.T_AOPEN, "{ expected");
		
		ParsedVariant result = new ParsedVariant(pkg, position, modifiers, annotations, name, typeParameters, outerDefinition);
		
		int ordinal = 0;
		while (!tokens.isNext(ZSTokenType.T_ACLOSE) && !tokens.isNext(ZSTokenType.T_SEMICOLON)) {
			CodePosition optionPosition = tokens.getPosition();
			String optionName = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
			List<IParsedType> types = new ArrayList<>();
			if (tokens.optional(ZSTokenType.T_BROPEN) != null) {
				types.add(IParsedType.parse(tokens));
				while (tokens.optional(ZSTokenType.T_COMMA) != null) {
					types.add(IParsedType.parse(tokens));
				}
				tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
			}
			result.addVariant(new ParsedVariantOption(optionPosition, optionName, ordinal++, types));
			if (tokens.optional(ZSTokenType.T_COMMA) == null)
				break;
		}
		
		if (tokens.optional(ZSTokenType.T_SEMICOLON) != null) {
			try {
				while (tokens.optional(ZSTokenType.T_ACLOSE) == null) {
					result.addMember(ParsedDefinitionMember.parse(tokens, result, null));
				}
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilToken(ZSTokenType.T_ACLOSE);
			}
		} else {
			tokens.required(ZSTokenType.T_ACLOSE, "} expected");
		}
		return result;
	}
	
	private final List<ParsedTypeParameter> typeParameters;
	private final List<ParsedVariantOption> variants = new ArrayList<>();
	
	private final VariantDefinition compiled;
	
	public ParsedVariant(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> typeParameters, HighLevelDefinition outerDefinition) {
		super(position, modifiers, pkg, annotations);
		
		this.typeParameters = typeParameters;
		compiled = new VariantDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outerDefinition);
		compiled.setTypeParameters(ParsedTypeParameter.getCompiled(typeParameters));
	}
	
	public void addVariant(ParsedVariantOption value) {
		variants.add(value);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}
	
	@Override
	public void linkTypesLocal(TypeResolutionContext context) {
		ParsedTypeParameter.compile(context, compiled.typeParameters, typeParameters);
		for (ParsedVariantOption variant : variants) {
			compiled.options.add(variant.compile(compiled, context));
		}
		
		super.linkTypesLocal(context);
	}
}
