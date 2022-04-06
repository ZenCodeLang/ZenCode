package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;

public class ParsedExpressionConditional extends ParsedExpression {
	private final ParsedExpression condition;
	private final ParsedExpression ifThen;
	private final ParsedExpression ifElse;

	public ParsedExpressionConditional(CodePosition position, ParsedExpression condition, ParsedExpression ifThen, ParsedExpression ifElse) {
		super(position);

		this.condition = condition;
		this.ifThen = ifThen;
		this.ifElse = ifElse;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression condition = this.condition.compile(compiler);
		CompilingExpression ifThen = this.ifThen.compile(compiler);
		CompilingExpression ifElse = this.ifElse.compile(compiler);
		return new Compiling(compiler, position, condition, ifThen, ifElse);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression condition;
		private final CompilingExpression ifThen;
		private final CompilingExpression ifElse;

		public Compiling(
				ExpressionCompiler compiler,
				CodePosition position,
				CompilingExpression condition,
				CompilingExpression ifThen,
				CompilingExpression ifElse
		) {
			super(compiler, position);
			this.condition = condition;
			this.ifThen = ifThen;
			this.ifElse = ifElse;
		}

		@Override
		public Expression as(TypeID type) {
			Expression condition = this.condition.as(BasicTypeID.BOOL);
			Expression ifThen = this.ifThen.as(type);
			Expression ifElse = this.ifElse.as(type);
			return compiler.at(position, type).ternary(condition, ifThen, ifElse);
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return TypeMatch.min(ifThen.matches(returnType), ifElse.matches(returnType));
		}

		@Override
		public InferredType inferType() {
			InferredType thenType = ifThen.inferType();
			InferredType elseType = ifElse.inferType();
			if (thenType.isFailed())
				return thenType;
			if (elseType.isFailed())
				return elseType;

			return compiler.union(thenType.get(), elseType.get())
					.map(InferredType::success)
					.orElseGet(() -> InferredType.failure(
							CompileExceptionCode.TYPE_CANNOT_UNITE,
							thenType.get() + " and " + elseType.get() + " are incompatible"));
		}
	}
}
