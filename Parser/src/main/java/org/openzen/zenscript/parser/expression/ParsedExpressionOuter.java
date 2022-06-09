package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class ParsedExpressionOuter extends ParsedExpression {
	private final CompilableExpression value;

	public ParsedExpressionOuter(CodePosition position, CompilableExpression value) {
		super(position);

		this.value = value;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		Optional<TypeID> thisType = compiler.getThisType();
		if (!thisType.isPresent())
			return compiler.invalid(position, CompileErrors.noThisInScope());

		throw new UnsupportedOperationException("Not yet supported");
	}
}
