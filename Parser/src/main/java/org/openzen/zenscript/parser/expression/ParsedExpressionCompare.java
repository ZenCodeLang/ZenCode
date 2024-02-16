package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class ParsedExpressionCompare extends ParsedExpression {
	private final CompilableExpression left;
	private final CompilableExpression right;
	private final CompareType type;

	public ParsedExpressionCompare(
			CodePosition position,
			CompilableExpression left,
			CompilableExpression right,
			CompareType type) {
		super(position);

		this.left = left;
		this.right = right;
		this.type = type;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression left = this.left.compile(compiler);
		CompilingExpression right = this.right.compile(compiler);
		return new Compiling(compiler, position, left, right, type);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression left;
		private final CompilingExpression right;
		private final CompareType type;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression left, CompilingExpression right, CompareType type) {
			super(compiler, position);
			this.left = left;
			this.right = right;
			this.type = type;
		}

		@Override
		public Expression eval() {
			Expression left = this.left.eval();
			ResolvedType resolved = compiler.resolve(left.type);

			if (type == CompareType.EQ) {
				Optional<InstanceCallable> equals = resolved.findOperator(OperatorType.EQUALS);
				if (equals.isPresent()) {
					return equals.get().call(compiler, position, left, TypeID.NONE, right);
				}
			} else if (type == CompareType.NE) {
				Optional<InstanceCallable> notEquals = resolved.findOperator(OperatorType.NOTEQUALS);
				if (notEquals.isPresent()) {
					return notEquals.get().call(compiler, position, left, TypeID.NONE, right);
				}
			}
			return resolved.compare(right.eval().type)
					.map(comparator -> comparator.compare(compiler, position, left, right, this.type))
					.orElseGet(() -> compiler.at(position).invalid(
							CompileErrors.noOperatorInType(left.type, OperatorType.COMPARE), //TODO Make error message more descriptive and include target type.
							BasicTypeID.BOOL));
		}

		@Override
		public CastedExpression cast(CastedEval eval) {
			return eval.of(eval());
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			left.collect(collector);
			right.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			left.linkVariables(linker);
			right.linkVariables(linker);
		}
	}
}
