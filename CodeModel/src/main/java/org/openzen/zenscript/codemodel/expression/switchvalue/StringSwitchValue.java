package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.Collections;
import java.util.List;

public class StringSwitchValue implements SwitchValue {
	public final String value;

	public StringSwitchValue(String value) {
		this.value = value;
	}

	@Override
	public List<VarStatement> getBindings() {
		return Collections.emptyList();
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptString(this);
	}

	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptString(context, this);
	}

	@Override
	public void collect(SSAVariableCollector collector) {

	}
}
