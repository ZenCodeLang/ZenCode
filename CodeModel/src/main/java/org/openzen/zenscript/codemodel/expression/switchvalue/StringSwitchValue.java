package org.openzen.zenscript.codemodel.expression.switchvalue;

public class StringSwitchValue implements SwitchValue {
	public final String value;
	
	public StringSwitchValue(String value) {
		this.value = value;
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptString(this);
	}
	
	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptString(context, this);
	}
}
