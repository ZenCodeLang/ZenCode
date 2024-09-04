package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.MemoizedExpression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;

public class MemoizedCompilingExpression extends AbstractCompilingExpression {
	private final CompilingExpression expression;
	private MemoizedExpression result;

	public MemoizedCompilingExpression(ExpressionCompiler compiler, CodePosition position, CompilingExpression expression) {
		super(compiler, position);

		this.expression = expression;
	}

	@Override
	public Expression eval() {
		if (result == null) {
			result = new MemoizedExpression(expression.eval());
		} else {
			result.setAccessedMoreThanOnce();
		}
		return result;
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		expression.collect(collector);
	}

	@Override
	public void linkVariables(CodeBlockStatement.VariableLinker linker) {
		expression.linkVariables(linker);
	}
}
