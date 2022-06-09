package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;

public class ErrorSwitchValue implements SwitchValue {
	public final CodePosition position;
	public final CompileError error;

	public ErrorSwitchValue(CodePosition position, CompileError error) {
		this.position = position;
		this.error = error;
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptError(this);
	}

	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptError(context, this);
	}
}
