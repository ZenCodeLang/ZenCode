package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.statement.VariableID;

public class SSAUnassigned implements SSAVariable {
	private final VariableID variable;

	public SSAUnassigned(VariableID variable) {
		this.variable = variable;
	}

	@Override
	public VariableID getVariable() {
		return variable;
	}
}
