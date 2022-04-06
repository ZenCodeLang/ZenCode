package org.openzen.zenscript.compiler.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;

public class InvalidCompilingExpression extends AbstractCompilingExpression {
	private final CompileExceptionCode code;
	private final String message;

	public InvalidCompilingExpression(ExpressionCompiler compiler, CodePosition position, CompileExceptionCode code, String message) {
		super(compiler, position);
		this.code = code;
		this.message = message;
	}

	@Override
	public Expression as(TypeID type) {
		return compiler.at(position, type).invalid(code, message);
	}

	@Override
	public TypeMatch matches(TypeID type) {
		return TypeMatch.NONE;
	}

	@Override
	public InferredType inferType() {
		return InferredType.failure(code, message);
	}
}
