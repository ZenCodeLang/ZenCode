package org.openzen.zenscript.compiler.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.ResolvedCallable;
import org.openzen.zenscript.compiler.types.ResolvedType;

import java.util.Optional;

public abstract class AbstractCompilingExpression implements CompilingExpression {
	protected final ExpressionCompiler compiler;
	protected final CodePosition position;

	public AbstractCompilingExpression(ExpressionCompiler compiler, CodePosition position) {
		this.compiler = compiler;
		this.position = position;
	}

	@Override
	public Expression eval() {
		InferredType type = inferType();
		if (type.isFailed()) {
			return compiler.at(position, BasicTypeID.UNDETERMINED).invalid(type.getErrorCode(), type.getErrorMessage());
		}

		return as(type.get());
	}

	@Override
	public Optional<ResolvedCallable> call() {
		return Optional.empty();
	}

	@Override
	public Optional<CompilingExpression> getMember(CodePosition position, GenericName name) {
		InferredType type = inferType();
		if (type.isFailed()) {
			return Optional.of(new InvalidCompilingExpression(compiler, position, type.getErrorCode(), type.getErrorMessage()));
		} else {
			Expression value = as(type.get());
			ResolvedType resolvedType = compiler.resolve(value.type);
			return resolvedType.findMemberGroup(name.name)
					.map(group -> new InstanceMemberGroupCompilingExpression(compiler, position, value, group));
		}
	}

	@Override
	public Expression assign(Expression value) {
		return compiler.at(position, value.type).invalidLValue();
	}
}
