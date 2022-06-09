package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementExpression extends ParsedStatement {
	private final CompilableExpression expression;

	public ParsedStatementExpression(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression expression) {
		super(position, annotations, whitespace);

		this.expression = expression;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		return result(new ExpressionStatement(position, compiler.compile(expression)), compiler);
	}
}
