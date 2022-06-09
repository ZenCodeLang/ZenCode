package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zenscript.codemodel.LocalVariable;

import java.util.List;

public interface SwitchValue {
	List<LocalVariable> getBindings();

	<T> T accept(SwitchValueVisitor<T> visitor);

	<C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor);
}
