package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.Collections;
import java.util.List;

public class CharSwitchValue implements SwitchValue {
	public final char value;

	public CharSwitchValue(char value) {
		this.value = value;
	}

	@Override
	public List<VarStatement> getBindings() {
		return Collections.emptyList();
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptChar(this);
	}

	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptChar(context, this);
	}

	@Override
	public void collect(SSAVariableCollector collector) {

	}
}
