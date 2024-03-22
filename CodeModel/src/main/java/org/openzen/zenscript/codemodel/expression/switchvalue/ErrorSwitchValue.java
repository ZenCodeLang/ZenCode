package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.VariableDefinition;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.Collections;
import java.util.List;

public class ErrorSwitchValue implements SwitchValue {
	public final CodePosition position;
	public final CompileError error;

	public ErrorSwitchValue(CodePosition position, CompileError error) {
		this.position = position;
		this.error = error;
	}

	@Override
	public List<VariableDefinition> getBindings() {
		return Collections.emptyList();
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptError(this);
	}

	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptError(context, this);
	}

	@Override
	public void collect(SSAVariableCollector collector) {

	}
}
