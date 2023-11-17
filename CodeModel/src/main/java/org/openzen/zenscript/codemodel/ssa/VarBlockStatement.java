package org.openzen.zenscript.codemodel.ssa;

import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingVariable;

public class VarBlockStatement implements CodeBlockStatement {
	private final CompilingExpression initializer;
	private final CompilingVariable compilingVariable;

	public VarBlockStatement(CompilingExpression initializer, CompilingVariable compilingVariable) {
		this.initializer = initializer;
		this.compilingVariable = compilingVariable;
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		if (initializer != null) {
			initializer.collect(collector);
			collector.assign(compilingVariable.id, () -> initializer);
		}
	}

	@Override
	public void linkVariables(VariableLinker linker) {
		compilingVariable.ssaCompilingVariable = linker.get(compilingVariable.id);
		if (initializer != null) {
			initializer.linkVariables(linker);
		}
	}
}
