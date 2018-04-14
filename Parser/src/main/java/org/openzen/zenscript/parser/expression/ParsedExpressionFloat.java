/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.ConstantDoubleExpression;
import org.openzen.zenscript.codemodel.expression.ConstantFloatExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionFloat extends ParsedExpression {
	public final double value;
	
	public ParsedExpressionFloat(CodePosition position, double value) {
		super(position);
		
		this.value = value;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		if (scope.hints.isEmpty())
			return new ConstantDoubleExpression(position, value);
		
		for (ITypeID hint : scope.hints) {
			if (hint == BasicTypeID.DOUBLE)
				return new ConstantDoubleExpression(position, value);
			else if (hint == BasicTypeID.FLOAT)
				return new ConstantFloatExpression(position, (float) value);
		}
		
		StringBuilder types = new StringBuilder();
		for (int i = 0; i < scope.hints.size(); i++) {
			if (i > 0)
				types.append(", ");
			
			types.append(scope.hints.get(i).toString());
		}
		
		throw new CompileException(position, CompileExceptionCode.INVALID_CAST, "Cannot cast a floating-point value to any of these types: " + types);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
