/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class SetLocalVariableExpression extends Expression {
	public final VarStatement variable;
	public final Expression value;
	
	public SetLocalVariableExpression(CodePosition position, VarStatement variable, Expression value) {
		super(position, variable.type);
		
		this.variable = variable;
		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitSetLocalVariable(this);
	}
}
