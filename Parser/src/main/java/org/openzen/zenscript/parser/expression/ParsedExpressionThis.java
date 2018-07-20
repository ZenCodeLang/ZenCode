/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.PrecompilationState;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionThis extends ParsedExpression {
	public ParsedExpressionThis(CodePosition position) {
		super(position);
	}
	
	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new ThisExpression(position, scope.getThisType());
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}

	@Override
	public ITypeID precompileForType(ExpressionScope scope, PrecompilationState state) {
		return scope.getThisType();
	}
}
