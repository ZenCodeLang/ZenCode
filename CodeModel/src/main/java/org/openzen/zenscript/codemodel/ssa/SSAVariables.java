package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.statement.VariableID;

import java.util.HashMap;
import java.util.Map;

public final class SSAVariables {
	private final Map<VariableID, SSAVariable> variables;

	public SSAVariables() {
		variables = new HashMap<>();
	}

	public SSAVariables(SSAVariables other) {
		variables = new HashMap<>(other.variables);
	}

	public void set(SSAVariable value) {
		variables.put(value.getVariable(), value);
	}

	public SSAVariable get(VariableID variable) {
		SSAVariable result = variables.get(variable);
		return result == null ? new SSAUnassigned(variable) : result;
	}
}
