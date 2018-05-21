/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

/**
 *
 * @author Hoofdgebruiker
 */
public interface CapturedExpressionVisitor<T> {
	T visitCapturedThis(CapturedThisExpression expression);
	
	T visitCapturedParameter(CapturedParameterExpression expression);
	
	T visitCapturedLocal(CapturedLocalVariableExpression expression);
	
	T visitCapturedDirect(CapturedDirectExpression expression);
	
	T visitRecaptured(CapturedClosureExpression expression);
}
