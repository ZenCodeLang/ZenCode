package org.openzen.zenscript.codemodel.expression.switchvalue;

public class CharSwitchValue implements SwitchValue {
	public final char value;
	
	public CharSwitchValue(char value) {
		this.value = value;
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptChar(this);
	}
	
	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptChar(context, this);
	}
}
