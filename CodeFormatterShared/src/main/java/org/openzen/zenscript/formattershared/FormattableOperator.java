/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formattershared;

/**
 *
 * @author Hoofdgebruiker
 */
public interface FormattableOperator {
	/**
	 * Operator priority: if priority of the inner operation is lower, it should
	 * be wrapped in parenthesis.
	 * 
	 * @return 
	 */
	int getPriority();
	
	String getOperatorString();
	
	public static boolean shouldWrapLeft(FormattableOperator inner, FormattableOperator outer) {
		return inner != outer && inner.getPriority() <= outer.getPriority();
	}
	
	public static boolean shouldWrapRight(FormattableOperator inner, FormattableOperator outer) {
		return inner.getPriority() <= outer.getPriority();
	}
}
