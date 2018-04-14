/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenStream;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.GenericFunctionScope;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedGenericParameter {
	public static ParsedGenericParameter parse(ZSTokenStream tokens) {
		ZSToken name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected");
		List<ParsedGenericBound> bounds = new ArrayList<>();
		while (tokens.optional(ZSTokenType.T_COLON) != null) {
			if (tokens.optional(ZSTokenType.K_SUPER) != null) {
				bounds.add(new ParsedSuperBound(IParsedType.parse(tokens)));
			} else {
				bounds.add(new ParsedTypeBound(tokens.getPosition(), IParsedType.parse(tokens)));
			}
		}
		return new ParsedGenericParameter(name.position, name.content, bounds);
	}
	
	public static List<ParsedGenericParameter> parseAll(ZSTokenStream tokens) {
		List<ParsedGenericParameter> genericParameters = new ArrayList<>();
		if (tokens.optional(ZSTokenType.T_LESS) != null) {
			do {
				genericParameters.add(ParsedGenericParameter.parse(tokens));
			} while (tokens.optional(ZSTokenType.T_COMMA) != null);
			tokens.required(ZSTokenType.T_GREATER, "> expected");
		}
		return genericParameters;
	}
	
	public static TypeParameter[] compile(BaseScope scope, List<ParsedGenericParameter> parameters) {
		TypeParameter[] result = new TypeParameter[parameters.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = parameters.get(i).compiled;
		
		GenericFunctionScope innerScope = new GenericFunctionScope(scope, result);
		for (int i = 0; i < result.length; i++) {
			for (ParsedGenericBound bound : parameters.get(i).bounds)
				result[i].addBound(bound.compile(innerScope));
		}
		
		return result;
	}
	
	public final CodePosition position;
	public final String name;
	public final List<ParsedGenericBound> bounds;
	
	public final TypeParameter compiled;
	
	public ParsedGenericParameter(CodePosition position, String name, List<ParsedGenericBound> bounds) {
		this.position = position;
		this.name = name;
		this.bounds = bounds;
		
		compiled = new TypeParameter(position, name);
	}
}
