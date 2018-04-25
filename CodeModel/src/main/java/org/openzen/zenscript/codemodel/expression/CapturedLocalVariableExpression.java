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
public class CapturedLocalVariableExpression extends CapturedExpression {
	public final VarStatement variable;
	
	public CapturedLocalVariableExpression(CodePosition position, VarStatement variable, LambdaClosure closure) {
		super(position, variable.type, closure);
		
		this.variable = variable;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitCapturedLocalVariable(this);
	}
}