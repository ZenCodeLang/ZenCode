/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.scope.GenericFunctionScope;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeParameter {
	public static ParsedTypeParameter parse(ZSTokenParser tokens) {
		CodePosition position = tokens.getPosition();
		ZSToken name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected");
		List<ParsedGenericBound> bounds = new ArrayList<>();
		while (tokens.optional(ZSTokenType.T_COLON) != null) {
			if (tokens.optional(ZSTokenType.K_SUPER) != null) {
				bounds.add(new ParsedSuperBound(IParsedType.parse(tokens)));
			} else {
				bounds.add(new ParsedTypeBound(tokens.getPosition(), IParsedType.parse(tokens)));
			}
		}
		return new ParsedTypeParameter(position, name.content, bounds);
	}
	
	public static List<ParsedTypeParameter> parseAll(ZSTokenParser tokens) {
		if (tokens.optional(ZSTokenType.T_LESS) == null)
			return null;
		
		List<ParsedTypeParameter> genericParameters = new ArrayList<>();
		do {
			genericParameters.add(ParsedTypeParameter.parse(tokens));
		} while (tokens.optional(ZSTokenType.T_COMMA) != null);
		tokens.required(ZSTokenType.T_GREATER, "> expected");
		return genericParameters;
	}
	
	public static void compile(TypeResolutionContext context, TypeParameter[] compiled, List<ParsedTypeParameter> parameters) {
		if (compiled == null)
			return;
		
		for (int i = 0; i < compiled.length; i++) {
			for (ParsedGenericBound bound : parameters.get(i).bounds)
				compiled[i].addBound(bound.compile(context));
		}
	}
	
	public static TypeParameter[] getCompiled(List<ParsedTypeParameter> parameters) {
		if (parameters == null)
			return TypeParameter.NONE;
		
		TypeParameter[] result = new TypeParameter[parameters.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = parameters.get(i).compiled;
		return result;
	}
	
	public final CodePosition position;
	public final String name;
	public final List<ParsedGenericBound> bounds;
	
	public final TypeParameter compiled;
	
	public ParsedTypeParameter(CodePosition position, String name, List<ParsedGenericBound> bounds) {
		this.position = position;
		this.name = name;
		this.bounds = bounds;
		
		compiled = new TypeParameter(position, name);
	}
}
