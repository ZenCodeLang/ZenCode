/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionString extends ParsedExpression {
	public final String value;
	
	public ParsedExpressionString(CodePosition position, String value) {
		super(position);
		
		this.value = value;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		if (scope.hints.contains(BasicTypeID.CHAR) && value.length() == 1) {
			return new ConstantCharExpression(position, value.charAt(0));
		}
		
		return new ConstantStringExpression(position, value);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
