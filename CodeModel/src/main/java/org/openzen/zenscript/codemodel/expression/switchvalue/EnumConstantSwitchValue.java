package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.Collections;
import java.util.List;

public class EnumConstantSwitchValue implements SwitchValue {
	public final EnumConstantMember constant;

	public EnumConstantSwitchValue(EnumConstantMember constant) {
		this.constant = constant;
	}

	@Override
	public List<VarStatement> getBindings() {
		return Collections.emptyList();
	}

	@Override
	public <T> T accept(SwitchValueVisitor<T> visitor) {
		return visitor.acceptEnumConstant(this);
	}

	@Override
	public <C, R> R accept(C context, SwitchValueVisitorWithContext<C, R> visitor) {
		return visitor.acceptEnumConstant(context, this);
	}

	@Override
	public void collect(SSAVariableCollector collector) {

	}
}
