package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementLock extends ParsedStatement {
	public final CompilableExpression object;
	public final ParsedStatement content;

	public ParsedStatementLock(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression object, ParsedStatement content) {
		super(position, annotations, whitespace);
		this.object = object;
		this.content = content;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		Expression object = compiler.compile(this.object);
		Statement content = this.content.compile(compiler.forBlock());
		return result(new LockStatement(position, object, content), compiler);
	}
}
