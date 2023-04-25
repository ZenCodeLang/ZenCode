package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.compilation.expression.SSACompilingVariable;
import org.openzen.zenscript.codemodel.statement.VariableID;

public interface CodeBlockStatement {
	void collect(SSAVariableCollector collector);

	void linkVariables(VariableLinker linker);

	interface VariableLinker {
		SSACompilingVariable get(VariableID variable);
	}
}
