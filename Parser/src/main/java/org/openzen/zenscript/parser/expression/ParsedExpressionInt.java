/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.ConstantByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantLongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantSByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantShortExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantULongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUShortExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionInt extends ParsedExpression {
	public final long value;
	
	public ParsedExpressionInt(CodePosition position, long value) {
		super(position);
		
		this.value = value;
	}

	@Override
	public Expression compile(ExpressionScope scope) {
		for (ITypeID hint : scope.hints) {
			if (hint instanceof BasicTypeID) {
				switch ((BasicTypeID) hint) {
					case SBYTE:
						return new ConstantSByteExpression(position, (byte) value);
					case BYTE:
						return new ConstantByteExpression(position, (byte) value);
					case SHORT:
						return new ConstantShortExpression(position, (short) value);
					case USHORT:
						return new ConstantUShortExpression(position, (short) value);
					case INT:
						return new ConstantIntExpression(position, (int) value);
					case UINT:
						return new ConstantUIntExpression(position, (int) value);
					case LONG:
						return new ConstantLongExpression(position, value);
					case ULONG:
						return new ConstantULongExpression(position, value);
					case CHAR:
						return new ConstantCharExpression(position, (char) value);
					default:
				}
			}
		}
		
		if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE)
			return new ConstantIntExpression(position, (int) value);
		else
			return new ConstantLongExpression(position, value);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
