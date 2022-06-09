package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zencode.shared.CodePosition;

public class ParsedDollarExpression extends ParsedExpression {
	public ParsedDollarExpression(CodePosition position) {
		super(position);
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return compiler.dollar().orElseGet(() -> compiler.invalid(position, CompileErrors.noDollarHere()));
	}
}
