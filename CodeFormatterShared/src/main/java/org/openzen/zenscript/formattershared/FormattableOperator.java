package org.openzen.zenscript.formattershared;

public interface FormattableOperator {
	/**
	 * Operator priority: if priority of the inner operation is lower, it should
	 * be wrapped in parenthesis.
	 * 
	 * @return 
	 */
	int getPriority();
	
	String getOperatorString();
	
	static boolean shouldWrapLeft(FormattableOperator inner, FormattableOperator outer) {
		return inner != outer && inner.getPriority() <= outer.getPriority();
	}
	
	static boolean shouldWrapRight(FormattableOperator inner, FormattableOperator outer) {
		return inner.getPriority() <= outer.getPriority();
	}
}
