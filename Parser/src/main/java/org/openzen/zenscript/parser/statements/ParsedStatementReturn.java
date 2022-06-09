package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.Optional;

public class ParsedStatementReturn extends ParsedStatement {
	private final CompilableExpression expression;

	public ParsedStatementReturn(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression expression) {
		super(position, annotations, whitespace);

		this.expression = expression;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		Optional<FunctionHeader> maybeFunctionHeader = compiler.getFunctionHeader();
		if (!maybeFunctionHeader.isPresent())
			return new InvalidStatement(position, CompileErrors.returnOutsideFunction());

		if (expression == null)
			return new ReturnStatement(position, null);

		FunctionHeader functionHeader = maybeFunctionHeader.get();
		Expression value = compiler.compile(expression, functionHeader.getReturnType());
		return result(new ReturnStatement(position, value), compiler);
	}
}
