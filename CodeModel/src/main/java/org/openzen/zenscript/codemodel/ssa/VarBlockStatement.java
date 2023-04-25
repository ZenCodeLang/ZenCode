package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.compilation.CompilingExpression;

public class VarBlockStatement implements CodeBlockStatement {
	private final CompilingExpression initializer;

	public VarBlockStatement(CompilingExpression initializer) {
		this.initializer = initializer;
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		if (initializer != null) {
			initializer.collect(collector);
		}
	}

	@Override
	public void linkVariables(VariableLinker linker) {
		if (initializer != null) {
			initializer.linkVariables(linker);
		}
	}
}
