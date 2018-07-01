/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

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
		if (value.length() == 1) {
			if (scope.hints.contains(BasicTypeID.CHAR)) {
				return new ConstantCharExpression(position, value.charAt(0));
			} else if (!scope.hints.contains(BasicTypeID.STRING)) {
				if (scope.hints.contains(BasicTypeID.INT))
					return new ConstantCharExpression(position, value.charAt(0));
			}
		}
		
		return new ConstantStringExpression(position, value);
	}
	
	@Override
	public SwitchValue compileToSwitchValue(ITypeID type, ExpressionScope scope) {
		if (type == BasicTypeID.CHAR) {
			if (value.length() != 1)
				throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "char value expected but string given");
			
			return new CharSwitchValue(value.charAt(0));
		} else if (type == BasicTypeID.STRING) {
			return new StringSwitchValue(value);
		} else {
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Can only use string keys for string values");
		}
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
