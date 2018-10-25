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
import org.openzen.zenscript.codemodel.scope.BaseScope.DollarEvaluator;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedDollarExpression extends ParsedExpression {
	public ParsedDollarExpression(CodePosition position) {
		super(position);
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		DollarEvaluator dollar = scope.getDollar();
		if (dollar == null)
			throw new CompileException(
					position,
					CompileExceptionCode.NO_DOLLAR_HERE,
					"No dollar expression available in this context");
		
		return dollar.apply(position);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
