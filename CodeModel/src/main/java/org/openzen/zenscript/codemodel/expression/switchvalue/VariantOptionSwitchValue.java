package org.openzen.zenscript.codemodel.expression.switchvalue;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.VariableDefinition;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionInstance;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;

import java.util.ArrayList;
import java.util.List;

public class VariantOptionSwitchValue implements SwitchValue {
	public final VariantOptionInstance option;
	public final String[] parameters;
	private final List<VariableDefinition> bindings;

	public VariantOptionSwitchValue(VariantOptionInstance option, List<VariableDefinition> bindings) {
		this.option = option;
		this.parameters = bindings.stream().map(p -> p.name).toArray(String[]::new);
		this.bindings = bindings;
	}

	@Override
	public List<VariableDefinition> getBindings() {
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

	@Override
	public void collect(SSAVariableCollector collector) {
	}
}
