package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.type.TypeID;

public class CompilingThisExpression extends AbstractCompilingExpression {
	private final TypeID thisType;

	public CompilingThisExpression(ExpressionCompiler compiler, CodePosition position, TypeID thisType) {
		super(compiler, position);
		this.thisType = thisType;
	}

	@Override
	public Expression eval() {
		return compiler.at(position).getThis(thisType);
	}

	@Override
	public void collect(SSAVariableCollector collector) {

	}

	@Override
	public void linkVariables(CodeBlockStatement.VariableLinker linker) {

	}
}
