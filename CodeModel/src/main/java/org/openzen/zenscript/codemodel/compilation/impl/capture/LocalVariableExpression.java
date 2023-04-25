package org.openzen.zenscript.codemodel.compilation.impl.capture;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.InstanceMemberCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.SSACompilingVariable;
import org.openzen.zenscript.codemodel.expression.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.CapturedLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.ssa.*;

public class LocalVariableExpression implements LocalExpression {
	private final CodePosition position;
	private final CompilingVariable variable;

	public LocalVariableExpression(CodePosition position, CompilingVariable variable) {
		this.position = position;
		this.variable = variable;
	}

	@Override
	public LocalExpression capture(LambdaClosure closure) {
		CapturedExpression value = new CapturedLocalVariableExpression(position, variable.complete(null /* TODO */), closure);
		return new LocalCapturedExpression(value);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new LocalVariableCompiling(compiler, position, variable);
	}

	private static class LocalVariableCompiling extends AbstractCompilingExpression implements SSAVariableUsage {
		private final CompilingVariable variable;
		private SSACompilingVariable ssaVariable;

		public LocalVariableCompiling(ExpressionCompiler compiler, CodePosition position, CompilingVariable variable) {
			super(compiler, position);
			this.variable = variable;
		}

		@Override
		public Expression eval() {
			if (ssaVariable == null)
				throw new IllegalStateException("SSA variable not set");
			if (variable.type == null)
				return compiler.at(position).invalid(CompileErrors.localVaribaleTypeUnknown(variable.name));

			return compiler.at(position).getLocalVariable(variable.complete(ssaVariable.as(variable.type)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			if (ssaVariable == null)
				throw new IllegalStateException("SSA variable not set");

			return cast.of(compiler.at(position).getLocalVariable(variable.complete(ssaVariable.as(cast.type))));
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return new InstanceMemberCompilingExpression(compiler, position, eval(), name);
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return new LocalVariableCompilingAssignment(compiler, position, variable, value);
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			collector.usage(variable.id, this);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			ssaVariable = linker.get(variable.id);
		}

		@Override
		public void set(SSACompilingVariable variable) {
			this.ssaVariable = variable;
		}
	}

	private static class LocalVariableCompilingAssignment extends AbstractCompilingExpression implements SSAVariableAssignment {
		private final CompilingVariable variable;
		private final CompilingExpression value;
		private SSACompilingVariable ssaVariable;

		public LocalVariableCompilingAssignment(
				ExpressionCompiler compiler,
				CodePosition position,
				CompilingVariable variable,
				CompilingExpression value
		) {
			super(compiler, position);
			this.variable = variable;
			this.value = value;
		}

		@Override
		public Expression eval() {
			if (ssaVariable == null)
				throw new IllegalStateException("SSA variable not set!");

			CastedEval cast = CastedEval.implicit(compiler, position, variable.type);
			return compiler.at(position).setLocalVariable(variable.complete(ssaVariable.as(variable.type)), value.cast(cast).value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			if (ssaVariable == null)
				throw new IllegalStateException("SSA variable not set!");

			return cast.of(compiler.at(position).setLocalVariable(variable.complete(ssaVariable.as(cast.type)), value.cast(cast).value));
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return new InstanceMemberCompilingExpression(compiler, position, eval(), name);
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			collector.assign(variable.id, this);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			ssaVariable = linker.get(variable.id);
		}

		@Override
		public CompilingExpression get() {
			return value;
		}
	}
}
