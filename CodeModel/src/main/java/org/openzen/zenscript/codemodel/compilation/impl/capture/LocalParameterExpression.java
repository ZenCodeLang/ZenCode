package org.openzen.zenscript.codemodel.compilation.impl.capture;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.CapturedParameterExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;

public class LocalParameterExpression implements LocalExpression {
	private final CodePosition position;
	private final FunctionParameter parameter;

	public LocalParameterExpression(CodePosition position, FunctionParameter parameter) {
		this.position = position;
		this.parameter = parameter;
	}

	@Override
	public LocalExpression capture(LambdaClosure closure) {
		CapturedExpression value = new CapturedParameterExpression(position, parameter, closure);
		closure.add(value);
		return new LocalCapturedExpression(value);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new ParameterCompiling(compiler, position, parameter);
	}

	private static class ParameterCompiling extends AbstractCompilingExpression {
		private final FunctionParameter parameter;

		public ParameterCompiling(ExpressionCompiler compiler, CodePosition position, FunctionParameter parameter) {
			super(compiler, position);

			this.parameter = parameter;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).getFunctionParameter(parameter);
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return new SetParameterCompiling(compiler, position, parameter, value);
		}
	}

	private static class SetParameterCompiling extends AbstractCompilingExpression {
		private final FunctionParameter parameter;
		private final CompilingExpression value;

		public SetParameterCompiling(ExpressionCompiler compiler, CodePosition position, FunctionParameter parameter, CompilingExpression value) {
			super(compiler, position);

			this.parameter = parameter;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).setFunctionParameter(parameter, value.cast(cast(parameter.type)).value);
		}
	}
}
