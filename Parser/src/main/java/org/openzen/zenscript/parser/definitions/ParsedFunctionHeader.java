/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.context.LocalTypeResolutionContext;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFunctionHeader {
	public static ParsedFunctionHeader parse(ZSTokenParser tokens) {
		List<ParsedTypeParameter> genericParameters = null;
		if (tokens.optional(ZSTokenType.T_LESS) != null) {
			genericParameters = new ArrayList<>();
			do {
				genericParameters.add(ParsedTypeParameter.parse(tokens));
			} while (tokens.optional(ZSTokenType.T_COMMA) != null);
			tokens.required(ZSTokenType.T_GREATER, "> expected");
		}
		
		tokens.required(T_BROPEN, "( expected");

		List<ParsedFunctionParameter> parameters = new ArrayList<>();
		if (tokens.optional(T_BRCLOSE) == null) {
			do {
				ParsedAnnotation[] annotations = ParsedAnnotation.parseAnnotations(tokens);
				ZSToken argName = tokens.required(T_IDENTIFIER, "identifier expected");
				boolean variadic = tokens.optional(T_DOT3) != null;
				
				IParsedType type = ParsedTypeBasic.UNDETERMINED;
				if (tokens.optional(K_AS) != null) {
					type = IParsedType.parse(tokens);
				}
				ParsedExpression defaultValue = null;
				if (tokens.optional(T_ASSIGN) != null) {
					defaultValue = ParsedExpression.parse(tokens);
				}
				parameters.add(new ParsedFunctionParameter(annotations, argName.content, type, defaultValue, variadic));
			} while (tokens.optional(T_COMMA) != null);
			tokens.required(T_BRCLOSE, ") expected");
		}

		IParsedType returnType = ParsedTypeBasic.UNDETERMINED;
		if (tokens.optional(K_AS) != null) {
			returnType = IParsedType.parse(tokens);
		}
		
		IParsedType thrownType = null;
		if (tokens.optional(K_THROWS) != null) {
			thrownType = IParsedType.parse(tokens);
		}
		
		return new ParsedFunctionHeader(genericParameters, parameters, returnType, thrownType);
	}
	
	public final List<ParsedTypeParameter> genericParameters;
	public final List<ParsedFunctionParameter> parameters;
	public final IParsedType returnType;
	public final IParsedType thrownType;
	
	public ParsedFunctionHeader(List<ParsedFunctionParameter> parameters, IParsedType returnType, IParsedType thrownType) {
		this.genericParameters = Collections.emptyList();
		this.parameters = parameters;
		this.returnType = returnType;
		this.thrownType = thrownType;
	}
	
	public ParsedFunctionHeader(List<ParsedTypeParameter> genericParameters, List<ParsedFunctionParameter> parameters, IParsedType returnType, IParsedType thrownType) {
		this.genericParameters = genericParameters;
		this.parameters = parameters;
		this.returnType = returnType;
		this.thrownType = thrownType;
	}
	
	public FunctionHeader compile(TypeResolutionContext context) {
		TypeParameter[] genericParameters = ParsedTypeParameter.getCompiled(this.genericParameters);
		LocalTypeResolutionContext localContext = new LocalTypeResolutionContext(context, null, genericParameters);
		ParsedTypeParameter.compile(localContext, genericParameters, this.genericParameters);
		
		ITypeID returnType = this.returnType.compile(localContext);
		FunctionParameter[] parameters = new FunctionParameter[this.parameters.size()];
		for (int i = 0; i < parameters.length; i++)
			parameters[i] = this.parameters.get(i).compile(localContext);
		
		return new FunctionHeader(genericParameters, returnType, thrownType == null ? null : thrownType.compile(context), parameters);
	}
}
