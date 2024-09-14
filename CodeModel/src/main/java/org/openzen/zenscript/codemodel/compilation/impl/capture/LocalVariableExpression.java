package org.openzen.zenscript.codemodel.compilation.impl.capture;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.InstanceMemberCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.expression.SSACompilingVariable;
import org.openzen.zenscript.codemodel.expression.captured.CapturedLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableExpression;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableInvalidExpression;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableLocalVariableExpression;
import org.openzen.zenscript.codemodel.ssa.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

import java.util.Optional;

public class LocalVariableExpression implements LocalExpression {
	private final CodePosition position;
	private final CompilingVariable variable;

	public LocalVariableExpression(CodePosition position, CompilingVariable variable) {
		this.position = position;
		this.variable = variable;
	}

	@Override
	public LocalExpression capture(LambdaClosure closure) {
		CapturedLocalVariableExpression value = new CapturedLocalVariableExpression(position, variable.eval(), closure);
		closure.add(value);
		return new LocalCapturedExpression(value);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new LocalVariableCompiling(compiler, position, variable);
	}

	private static class LocalVariableCompiling extends AbstractCompilingExpression implements SSAVariableUsage {
		private final CompilingVariable variable;

		public LocalVariableCompiling(ExpressionCompiler compiler, CodePosition position, CompilingVariable variable) {
			super(compiler, position);
			this.variable = variable;
		}

		@Override
		public Expression eval() {
			if (variable.getActualType() == null)
				return compiler.at(position).invalid(CompileErrors.localVariableTypeUnknown(variable.name));

			return compiler.at(position).getLocalVariable(variable.eval());
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(compiler.at(position).getLocalVariable(variable.eval()));
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return new InstanceMemberCompilingExpression(compiler, position, this, name);
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return new LocalVariableCompilingAssignment(compiler, position, variable, value);
		}

		@Override
		public Optional<ModifiableExpression> asModifiable() {
			if (variable.getActualType() == null)
				return Optional.of(new ModifiableInvalidExpression(position, BasicTypeID.INVALID, CompileErrors.localVariableTypeUnknown(variable.name)));

			return Optional.of(new ModifiableLocalVariableExpression(position, variable.eval()));
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			collector.usage(variable.id, this);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			variable.ssaCompilingVariable = linker.get(variable.id);
		}

		@Override
		public void set(SSACompilingVariable variable) {
			this.variable.ssaCompilingVariable = variable;
		}
	}

	private static class LocalVariableCompilingAssignment extends AbstractCompilingExpression implements SSAVariableAssignment {
		private final CompilingVariable variable;
		private final CompilingExpression value;

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
			Expression value;
			if (variable.getActualType() == null) {
				value = this.value.eval();
			} else {
				CastedEval cast = CastedEval.implicit(compiler, position, variable.getActualType());
				value = this.value.cast(cast).value;
			}
			return compiler.at(position).setLocalVariable(variable.asType(value.type), value);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(compiler.at(position).setLocalVariable(variable.asType(cast.type), value.cast(cast).value));
		}

		@Override
		public CompilingExpression getMember(CodePosition position, GenericName name) {
			return new InstanceMemberCompilingExpression(compiler, position, this, name);
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			collector.assign(variable.id, this);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}

		@Override
		public CompilingExpression get() {
			return value;
		}
	}
}
