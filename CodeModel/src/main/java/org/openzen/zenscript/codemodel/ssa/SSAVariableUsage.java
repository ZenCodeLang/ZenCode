package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.compilation.expression.SSACompilingVariable;

public interface SSAVariableUsage {
	void set(SSACompilingVariable variable);
}
