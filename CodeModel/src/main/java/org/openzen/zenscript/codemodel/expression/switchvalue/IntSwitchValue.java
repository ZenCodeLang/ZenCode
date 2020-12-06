package org.openzen.zenscript.codemodel.expression.switchvalue;

public class IntSwitchValue implements SwitchValue {
	public final int value;

	public IntSwitchValue(int value) {
		this.value = value;
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptInt(this);
	}

	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptInt(context, this);
	}
}
