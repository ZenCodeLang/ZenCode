package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedExpressionUnary extends ParsedExpression {
	private final CompilableExpression value;
	private final OperatorType operator;

	public ParsedExpressionUnary(CodePosition position, CompilableExpression value, OperatorType operator) {
		super(position);

		this.value = value;
		this.operator = operator;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value.compile(compiler), operator);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression value;
		private final OperatorType operator;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value, OperatorType operator) {
			super(compiler, position);

			this.value = value;
			this.operator = operator;
		}

		@Override
		public Expression eval() {
			Expression value = this.value.eval();
			ResolvedType resolvedType = compiler.resolve(value.type);
			return resolvedType.findOperator(operator)
					.map(operator -> operator.call(compiler, position, value, TypeID.NONE))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.noOperatorInType(value.type, operator)));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			Expression value = this.value.eval();
			ResolvedType resolvedType = compiler.resolve(value.type);
			return resolvedType.findOperator(operator)
					.map(operator -> cast.of(operator.call(compiler, position, value, TypeID.NONE)))
					.orElseGet(() -> cast.invalid(CompileErrors.noOperatorInType(value.type, operator)));
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
