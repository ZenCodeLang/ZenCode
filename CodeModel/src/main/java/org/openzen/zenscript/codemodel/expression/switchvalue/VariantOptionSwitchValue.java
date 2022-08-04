package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionInstance;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;

import java.util.ArrayList;
import java.util.List;

public class VariantOptionSwitchValue implements SwitchValue {
	public final VariantOptionInstance option;
	public final String[] parameters;
	private final List<VarStatement> bindings;

	public VariantOptionSwitchValue(VariantOptionInstance option, String[] parameters) {
		this.option = option;
		this.parameters = parameters;
		this.bindings = new ArrayList<>();
		for (int i = 0; i < option.types.length; i++) {
			bindings.add(new VarStatement(
					CodePosition.UNKNOWN,
					new VariableID(),
					parameters[i],
					option.types[i],
					null,
					true));
		}
	}

	@Override
	public List<VarStatement> getBindings() {
		return bindings;
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
