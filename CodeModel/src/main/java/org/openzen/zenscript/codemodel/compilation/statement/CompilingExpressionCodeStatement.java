package org.openzen.zenscript.codemodel.compilation.statement;

import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;

public class CompilingExpressionCodeStatement implements CodeBlockStatement {
	private final CompilingExpression expression;

	public CompilingExpressionCodeStatement(CompilingExpression expression) {
		this.expression = expression;
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		expression.collect(collector);
	}

	@Override
	public void linkVariables(VariableLinker linker) {
		expression.linkVariables(linker);
	}
}
