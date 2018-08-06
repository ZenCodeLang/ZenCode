/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionOuter extends ParsedExpression {
	private final ParsedExpression value;
	
	public ParsedExpressionOuter(CodePosition position, ParsedExpression value) {
		super(position);
		
		this.value = value;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID thisType = scope.getThisType();
		if (thisType == null || !(thisType instanceof DefinitionTypeID))
			throw new CompileException(position, CompileExceptionCode.USING_THIS_OUTSIDE_TYPE, "Not in a type");
		
		return scope.getOuterInstance(position);
	}

	@Override
	public boolean hasStrongType() {
		return value.hasStrongType();
	}
}
