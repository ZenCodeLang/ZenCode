/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ThrowExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedThrowExpression extends ParsedExpression {
	public final ParsedExpression value;
	
	public ParsedThrowExpression(CodePosition position, ParsedExpression value) {
		super(position);
		
		this.value = value;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cValue = value.compile(scope).eval();
		ITypeID resultType = BasicTypeID.VOID;
		if (scope.getResultTypeHints().size() == 1)
			resultType = scope.getResultTypeHints().get(0);
		
		return new ThrowExpression(position, resultType, cValue);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
