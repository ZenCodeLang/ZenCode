package org.openzen.zenscript.codemodel.compilation.impl.capture;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

public class LocalThisExpression implements LocalExpression {
	private final CodePosition position;
	private final TypeID type;

	public LocalThisExpression(CodePosition position, TypeID type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public LocalExpression capture(LambdaClosure closure) {
		CapturedThisExpression value = new CapturedThisExpression(position, type, closure);
		closure.add(value);
		return new LocalCapturedExpression(value);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, type);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final TypeID type;

		public Compiling(ExpressionCompiler compiler, CodePosition position, TypeID type) {
			super(compiler, position);

			this.type = type;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).getThis(type);
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return this;
		}

		@Override
		public void collect(SSAVariableCollector collector) {}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {}
	}
}
