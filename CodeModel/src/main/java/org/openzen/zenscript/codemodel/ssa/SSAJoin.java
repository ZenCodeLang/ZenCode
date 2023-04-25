package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.statement.VariableID;

public class SSAJoin implements SSAVariable {
	public final VariableID variable;
	public final SSAVariable[] sources;

	public SSAJoin(VariableID variable, SSAVariable[] sources) {
		this.variable = variable;
		this.sources = sources;
	}

	@Override
	public VariableID getVariable() {
		return variable;
	}
}
