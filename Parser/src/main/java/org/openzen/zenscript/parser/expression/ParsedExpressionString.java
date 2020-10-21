/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionString extends ParsedExpression {
	public final String value;
	public final boolean singleQuote;
	
	public ParsedExpressionString(CodePosition position, String value, boolean singleQuote) {
		super(position);
		
		this.value = value;
		this.singleQuote = singleQuote;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		if (value.length() == 1 && (singleQuote || scope.hints.contains(BasicTypeID.CHAR)))
			return new ConstantCharExpression(position, value.charAt(0));
		
		return new ConstantStringExpression(position, value);
	}
	
	@Override
	public SwitchValue compileToSwitchValue(TypeID type, ExpressionScope scope) throws CompileException {
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
