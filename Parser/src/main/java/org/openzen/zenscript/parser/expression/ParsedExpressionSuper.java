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
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionSuper extends ParsedExpression {
	public ParsedExpressionSuper(CodePosition position) {
		super(position);
	}
	
	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID type = scope.getThisType();
		ITypeID targetType = type.getSuperType(scope.getTypeRegistry());
		if (targetType == null)
			throw new CompileException(position, CompileExceptionCode.SUPER_CALL_NO_SUPERCLASS, "Type has no superclass");
		
		return new PartialTypeExpression(position, targetType, null);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}

	@Override
	public ITypeID precompileForType(ExpressionScope scope, PrecompilationState state) {
		return scope.getThisType().getSuperType(scope.getTypeRegistry());
	}
}
