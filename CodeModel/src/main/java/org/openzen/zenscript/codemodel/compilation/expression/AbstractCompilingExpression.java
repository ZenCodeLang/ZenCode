package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.Expression;
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
		Expression value = eval();
		return compiler.resolve(value.type).findOperator(OperatorType.CALL).map(op -> new CompilingCallable() {
			@Override
			public Expression call(CodePosition position, CompilingExpression[] arguments) {
				return op.call(compiler, position, value, TypeID.NONE, arguments);
			}

			@Override
			public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
				return op.cast(compiler, position, cast, value, TypeID.NONE, arguments);
			}
		});
	}

	@Override
	public CompilingExpression getMember(CodePosition position, GenericName name) {
		return new InstanceMemberCompilingExpression(compiler, position, eval(), name);
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		return compiler.invalid(position, CompileErrors.invalidLValue());
	}

	@Override
	public Expression as(TypeID type) {
		return cast(CastedEval.implicit(compiler, position, type)).value;
	}

	protected CastedEval cast(TypeID returnType) {
		return CastedEval.implicit(compiler, position, returnType);
	}
}
