/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class CapturedExpression extends Expression {
	public final LambdaClosure closure;
	
	public CapturedExpression(CodePosition position, ITypeID type, LambdaClosure closure) {
		super(position, type);
		
		this.closure = closure;
	}
	
	@Override
	public CapturedExpression capture(CodePosition position, LambdaClosure closure) {
		CapturedExpression result = new CapturedClosureExpression(position, this, closure);
		closure.add(result);
		return result;
	}
}