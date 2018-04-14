/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

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
}
