package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public abstract class AbstractCompilingExpression implements CompilingExpression {
	protected final ExpressionCompiler compiler;
	protected final CodePosition position;

	public AbstractCompilingExpression(ExpressionCompiler compiler, CodePosition position) {
		this.compiler = compiler;
		this.position = position;
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return cast.of(eval());
	}

	@Override
	public Optional<CompilingCallable> call() {
		return Optional.empty();
	}

	@Override
	public CompilingExpression getMember(CodePosition position, GenericName name) {
		return new InstanceMemberCompilingExpression(compiler, position, eval(), name);
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		return compiler.invalid(position, CompileErrors.invalidLValue());
	}

	protected CastedEval cast(TypeID returnType) {
		return CastedEval.implicit(compiler, position, returnType);
	}
}
