package org.openzen.zenscript.formattershared;

public class ExpressionString {
	public final String value;
	public final FormattableOperator priority;
	
	public ExpressionString(String value, FormattableOperator priority) {
		this.value = value;
		this.priority = priority;
	}
	
	public String wrapLeft(FormattableOperator outer) {
		return FormattableOperator.shouldWrapLeft(priority, outer) ? "(" + value + ")" : value;
	}
	
	public String wrapRight(FormattableOperator outer) {
		return FormattableOperator.shouldWrapRight(priority, outer) ? "(" + value + ")" : value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	public static ExpressionString binary(ExpressionString left, ExpressionString right, FormattableOperator operator) {
		String value = left.wrapLeft(operator)
				+ operator.getOperatorString() + right.wrapRight(operator);
		return new ExpressionString(value, operator);
	}
	
	public ExpressionString unaryPrefix(FormattableOperator operator) {
		return new ExpressionString(operator.getOperatorString() + wrapRight(operator), operator);
	}
	
	public ExpressionString unaryPostfix(FormattableOperator operator) {
		return new ExpressionString(wrapLeft(operator) + operator.getOperatorString(), operator);
	}
	
	public ExpressionString unaryPrefix(FormattableOperator operator, String operatorString) {
		return new ExpressionString(operatorString + wrapRight(operator), operator);
	}
	
	public ExpressionString unaryPostfix(FormattableOperator operator, String operatorString) {
		return new ExpressionString(wrapLeft(operator) + operatorString, operator);
	}
}
