package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;

import java.util.Optional;

public class InvalidCompilingExpression extends AbstractCompilingExpression implements CompilingCallable {
	private final CompileError error;

	public InvalidCompilingExpression(ExpressionCompiler compiler, CodePosition position, CompileError error) {
		super(compiler, position);
		this.error = error;
	}

	@Override
	public Expression eval() {
		return compiler.at(position).invalid(error);
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.invalid(error);
	}

	@Override
	public Optional<CompilingCallable> call() {
		return Optional.of(this);
	}

	@Override
	public CompilingExpression getMember(CodePosition position, GenericName name) {
		return this;
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		return compiler.invalid(position, CompileErrors.invalidLValue());
	}

	@Override
	public void collect(SSAVariableCollector collector) {}

	@Override
	public void linkVariables(CodeBlockStatement.VariableLinker linker) {}

	@Override
	public Expression call(CodePosition position, CompilingExpression[] arguments) {
		return compiler.at(position).invalid(error);
	}

	@Override
	public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
		return CastedExpression.invalid(position, error);
	}
}
