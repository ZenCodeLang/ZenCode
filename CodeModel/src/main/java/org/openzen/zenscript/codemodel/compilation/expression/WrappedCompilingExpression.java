package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.compilation.CastedEval;
import org.openzen.zenscript.codemodel.compilation.CastedExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;

public class WrappedCompilingExpression extends AbstractCompilingExpression{
	private final Expression value;

	public WrappedCompilingExpression(ExpressionCompiler compiler, Expression value) {
		super(compiler, value.position);
		this.value = value;
	}

	@Override
	public Expression eval() {
		return value;
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.of(value);
	}

	@Override
	public void collect(SSAVariableCollector collector) {
		// TODO
		//value.collect(collector);
	}

	@Override
	public void linkVariables(CodeBlockStatement.VariableLinker linker) {}
}
