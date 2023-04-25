package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.VariableID;

public class SSAValue implements SSAVariable {
	public final VariableID variable;
	public final Expression value;

	public SSAValue(VariableID variable, Expression value) {
		this.variable = variable;
		this.value = value;
	}

	@Override
	public VariableID getVariable() {
		return variable;
	}
}
