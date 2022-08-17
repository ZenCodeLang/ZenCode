package org.openzen.zenscript.codemodel.compilation.impl.capture;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.CastedEval;
import org.openzen.zenscript.codemodel.compilation.CastedExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.InstanceMemberCompilingExpression;
import org.openzen.zenscript.codemodel.expression.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.CapturedLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.statement.VarStatement;

public class LocalVariableExpression implements LocalExpression {
	private final CodePosition position;
	private final VarStatement variable;

	public LocalVariableExpression(CodePosition position, VarStatement variable) {
		this.position = position;
		this.variable = variable;
	}

	@Override
	public LocalExpression capture(LambdaClosure closure) {
		CapturedExpression value = new CapturedLocalVariableExpression(position, variable, closure);
		return new LocalCapturedExpression(value);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new LocalVariableCompiling(compiler, position, variable);
	}

	private static class LocalVariableCompiling extends AbstractCompilingExpression {
		private final VarStatement variable;

		public LocalVariableCompiling(ExpressionCompiler compiler, CodePosition position, VarStatement variable) {
			super(compiler, position);
			this.variable = variable;
		}

		@Override
		public Expression eval() {
			return compiler.at(position).getLocalVariable(variable);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return new InstanceMemberCompilingExpression(compiler, position, eval(), name);
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return new LocalVariableCompilingAssignment(compiler, position, variable, value);
		}
	}

	private static class LocalVariableCompilingAssignment extends AbstractCompilingExpression {
		private final VarStatement variable;
		private final CompilingExpression value;

		public LocalVariableCompilingAssignment(
				ExpressionCompiler compiler,
				CodePosition position,
				VarStatement variable,
				CompilingExpression value
		) {
			super(compiler, position);
			this.variable = variable;
			this.value = value;
		}

		@Override
		public Expression eval() {
			CastedEval cast = CastedEval.implicit(compiler, position, variable.type);
			return compiler.at(position).setLocalVariable(variable, value.cast(cast).value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return new InstanceMemberCompilingExpression(compiler, position, eval(), name);
		}
	}
}
