package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zencode.shared.CodePosition;

public class ParsedExpressionAssign extends ParsedExpression {
	private final CompilableExpression left;
	private final CompilableExpression right;

	public ParsedExpressionAssign(CodePosition position, CompilableExpression left, CompilableExpression right) {
		super(position);

		this.left = left;
		this.right = right;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return left.compile(compiler).assign(right.compile(compiler));
	}
}
