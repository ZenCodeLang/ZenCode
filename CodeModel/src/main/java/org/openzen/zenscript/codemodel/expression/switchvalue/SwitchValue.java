package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zenscript.codemodel.LocalVariable;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.List;

public interface SwitchValue {
	List<VarStatement> getBindings();

	<T> T accept(SwitchValueVisitor<T> visitor);

	<C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor);

	void collect(SSAVariableCollector collector);
}
