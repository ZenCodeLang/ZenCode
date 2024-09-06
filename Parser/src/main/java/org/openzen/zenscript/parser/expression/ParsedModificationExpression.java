package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.InvalidCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ModificationExpression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;

public class ParsedModificationExpression extends ParsedExpression {
	private final CompilableExpression value;
	private final ModificationExpression.Modification modification;

	public ParsedModificationExpression(CodePosition position, CompilableExpression value, ModificationExpression.Modification modification) {
		super(position);

		this.value = value;
		this.modification = modification;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value.compile(compiler), modification);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression value;
		private final ModificationExpression.Modification modification;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value, ModificationExpression.Modification modification) {
			super(compiler, position);

			this.value = value;
			this.modification = modification;
		}

		@Override
		public Expression eval() {
			return value.asModifiable()
					.map(modifiable -> {
						return compiler.resolve(modifiable.getType()).findOperator(modification.operator)
								.map(operator -> operator.callModification(compiler.at(position), modifiable, modification))
								.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noOperatorInType(modifiable.getType(), modification.operator)));

					})
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.invalidModificationTarget()));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			value.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			value.linkVariables(linker);
		}
	}
}
