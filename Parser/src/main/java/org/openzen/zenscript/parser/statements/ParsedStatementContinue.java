package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.statement.ContinueStatement;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementContinue extends ParsedStatement {
	public final String name;

	public ParsedStatementContinue(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name) {
		super(position, annotations, whitespace);

		this.name = name;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		return compiler.getLoop(name)
				.map(loop -> result(new ContinueStatement(position, loop), compiler))
				.orElseGet(() -> new InvalidStatement(position, CompileErrors.continueOutsideLoop(name)));
	}
}
