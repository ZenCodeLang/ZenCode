package org.openzen.zenscript.codemodel.expression.switchvalue;

public interface SwitchValue {
	<T> T accept(SwitchValueVisitor<T> visitor);
	
	<C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor);
}
