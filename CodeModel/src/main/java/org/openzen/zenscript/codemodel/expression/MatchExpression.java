/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class MatchExpression extends Expression {
	public final Expression value;
	public final Case[] cases;
	
	public MatchExpression(CodePosition position, Expression value, ITypeID type, Case[] cases) {
		super(position, type, binaryThrow(position, value.thrownType, getThrownType(position, cases)));
		
		this.value = value;
		this.cases = cases;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitMatch(this);
	}
	
	public static class Case {
		public final SwitchValue key;
		public final FunctionExpression value;
		
		public Case(SwitchValue key, FunctionExpression value) {
			this.key = key;
			this.value = value;
		}
	}
	
	private static ITypeID getThrownType(CodePosition position, Case[] cases) {
		if (cases.length == 0)
			return null;
		
		ITypeID result = cases[0].value.thrownType;
		for (int i = 1; i < cases.length; i++)
			result = binaryThrow(position, result, cases[i].value.thrownType);
		
		return result;
	}
}
