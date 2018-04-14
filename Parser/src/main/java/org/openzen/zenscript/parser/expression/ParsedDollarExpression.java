/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.function.Function;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedDollarExpression extends ParsedExpression {
	public ParsedDollarExpression(CodePosition position) {
		super(position);
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Function<CodePosition, Expression> dollar = scope.getDollar();
		if (dollar == null)
			throw new CompileException(position, CompileExceptionCode.NO_DOLLAR_HERE, "No dollar expression available in this context");
		
		return dollar.apply(position);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
