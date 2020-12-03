package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;

public class VariantOptionSwitchValue implements SwitchValue {
	public final VariantOptionRef option;
	public final String[] parameters;
	
	public VariantOptionSwitchValue(VariantOptionRef option, String[] parameters) {
		this.option = option;
		this.parameters = parameters;
	}
	
	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptVariantOption(this);
	}
	
	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptVariantOption(context, this);
	}
}
