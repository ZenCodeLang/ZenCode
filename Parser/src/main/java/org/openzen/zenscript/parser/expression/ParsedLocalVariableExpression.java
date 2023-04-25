package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;

import java.util.Optional;

public class ParsedLocalVariableExpression extends ParsedExpression {
	private final String name;

	public ParsedLocalVariableExpression(CodePosition position, String name) {
		super(position);

		this.name = name;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		Optional<LocalType> localType = compiler.getLocalType();
		if (!localType.isPresent())
			return compiler.invalid(position, CompileErrors.noThisInScope());

		ResolvedType resolved = compiler.resolve(localType.get().getThisType());
		Optional<ResolvedType.Field> maybeField = resolved.findField(name);
		if (!maybeField.isPresent())
			return compiler.invalid(position, CompileErrors.noFieldInType(localType.get().getThisType(), name));

		ResolvedType.Field field = maybeField.get();
		if (field.isStatic()) {
			return new CompilingStatic(compiler, position, field);
		} else {
			Expression this_ = compiler.at(position).getThis(localType.get().getThisType());
			return new CompilingInstance(compiler, position, field, this_);
		}
	}

	private static class CompilingStatic extends AbstractCompilingExpression {
		private final ResolvedType.Field field;

		public CompilingStatic(ExpressionCompiler compiler, CodePosition position, ResolvedType.Field field) {
			super(compiler, position);
			this.field = field;
		}

		@Override
		public Expression eval() {
			return field.getStatic(compiler.at(position));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return new CompilingStaticAssign(compiler, position, field, value);
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}
	}

	private static class CompilingStaticAssign extends AbstractCompilingExpression {
		private final ResolvedType.Field field;
		private final CompilingExpression value;

		public CompilingStaticAssign(ExpressionCompiler compiler, CodePosition position, ResolvedType.Field field, CompilingExpression value) {
			super(compiler, position);

			this.field = field;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return field.setStatic(compiler.at(position), value.cast(cast(field.getType())).value);
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

	private static class CompilingInstance extends AbstractCompilingExpression {
		private final ResolvedType.Field field;
		private final Expression this_;

		public CompilingInstance(ExpressionCompiler compiler, CodePosition position, ResolvedType.Field field, Expression this_) {
			super(compiler, position);
			this.field = field;
			this.this_ = this_;
		}

		@Override
		public Expression eval() {
			return field.get(compiler.at(position), this_);
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}

		@Override
		public CompilingExpression assign(CompilingExpression value) {
			return new CompilingStaticAssign(compiler, position, field, value);
		}

		@Override
		public void collect(SSAVariableCollector collector) {}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {}
	}

	private static class CompilingInstanceAssign extends AbstractCompilingExpression {
		private final ResolvedType.Field field;
		private final Expression this_;
		private final CompilingExpression value;

		public CompilingInstanceAssign(ExpressionCompiler compiler, CodePosition position, ResolvedType.Field field, Expression this_, CompilingExpression value) {
			super(compiler, position);

			this.field = field;
			this.this_ = this_;
			this.value = value;
		}

		@Override
		public Expression eval() {
			return field.set(compiler.at(position), this_, value.cast(cast(field.getType())).value);
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
