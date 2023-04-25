package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.statement.VariableID;

public interface SSAVariableCollector {
	void assign(VariableID variable, SSAVariableAssignment value);

	void usage(VariableID variable, SSAVariableUsage usage);

	SSAVariableCollector conditional();
}
