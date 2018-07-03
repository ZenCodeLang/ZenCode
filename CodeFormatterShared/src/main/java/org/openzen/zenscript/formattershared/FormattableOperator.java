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
	int getPriority();
	
	boolean isCommutative();
	
	String getOperatorString();
	
	public static boolean shouldWrapLeft(FormattableOperator inner, FormattableOperator outer) {
		return inner != outer && inner.getPriority() <= outer.getPriority();
	}
	
	public static boolean shouldWrapRight(FormattableOperator inner, FormattableOperator outer) {
		return (inner == outer && inner.isCommutative()) || (inner.getPriority() <= outer.getPriority());
	}
}
