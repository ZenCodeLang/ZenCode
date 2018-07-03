/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionCast extends ParsedExpression {
	private final ParsedExpression value;
	private final IParsedType type;
	private final boolean optional;

	public ParsedExpressionCast(CodePosition position, ParsedExpression value, IParsedType type, boolean optional) {
		super(position);

		this.value = value;
		this.type = type;
		this.optional = optional;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID type = this.type.compile(scope);
		return value.compile(scope.withHint(type))
				.eval()
				.castExplicit(position, scope, type, optional);
	}
	
	@Override
	public ParsedFunctionHeader toLambdaHeader() {
		if (optional)
			throw new CompileException(position, CompileExceptionCode.LAMBDA_HEADER_INVALID, "Not a valid lambda header");
		
		ParsedFunctionHeader header = value.toLambdaHeader();
		if (header.returnType != ParsedTypeBasic.ANY)
			throw new CompileException(position, CompileExceptionCode.LAMBDA_HEADER_INVALID, "Lambda parameter already has a return type");
		
		return new ParsedFunctionHeader(header.genericParameters, header.parameters, type, null);
	}
	
	@Override
	public ParsedFunctionParameter toLambdaParameter() {
		if (optional)
			throw new CompileException(position, CompileExceptionCode.LAMBDA_HEADER_INVALID, "Not a valid lambda header");
		
		ParsedFunctionParameter parameter = value.toLambdaParameter();
		if (parameter.type != ParsedTypeBasic.ANY)
			throw new CompileException(position, CompileExceptionCode.LAMBDA_HEADER_INVALID, "Lambda parameter already has a type");
		
		return new ParsedFunctionParameter(ParsedAnnotation.NONE, parameter.name, type, null, false);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
