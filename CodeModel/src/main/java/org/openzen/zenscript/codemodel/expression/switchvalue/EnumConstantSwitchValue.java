package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zenscript.codemodel.member.EnumConstantMember;

public class EnumConstantSwitchValue implements SwitchValue {
	public final EnumConstantMember constant;
	
	public EnumConstantSwitchValue(EnumConstantMember constant) {
		this.constant = constant;
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptEnumConstant(this);
	}
	
	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptEnumConstant(context, this);
	}
}
