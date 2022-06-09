package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.Optional;

public class ParsedStatementThrow extends ParsedStatement {
	private final CompilableExpression expression;

	public ParsedStatementThrow(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression expression) {
		super(position, annotations, whitespace);

		this.expression = expression;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		Optional<FunctionHeader> maybeHeader = compiler.getFunctionHeader();
		if (!maybeHeader.isPresent())
			return new InvalidStatement(position, CompileErrors.cannotThrowHere());

		FunctionHeader header = maybeHeader.get();
		if (header.thrownType == null)
			return new InvalidStatement(position, CompileErrors.cannotThrowHere());

		return result(new ThrowStatement(position, compiler.compile(expression, header.thrownType)), compiler);
	}
}
