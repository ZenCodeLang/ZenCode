/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionString {
	public final String value;
	public final OperatorPriority priority;
	
	public ExpressionString(String value, OperatorPriority priority) {
		this.value = value;
		this.priority = priority;
	}
	
	public String wrapLeft(OperatorPriority outer) {
		return OperatorPriority.shouldWrapLeft(priority, outer) ? "(" + value + ")" : value;
	}
	
	public String wrapRight(OperatorPriority outer) {
		return OperatorPriority.shouldWrapRight(priority, outer) ? "(" + value + ")" : value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
