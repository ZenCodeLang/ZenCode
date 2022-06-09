package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementIf extends ParsedStatement {
	private final CompilableExpression condition;
	private final ParsedStatement onThen;
	private final ParsedStatement onElse;

	public ParsedStatementIf(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, CompilableExpression condition, ParsedStatement onThen, ParsedStatement onElse) {
		super(position, annotations, whitespace);

		this.condition = condition;
		this.onThen = onThen;
		this.onElse = onElse;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		Expression condition = compiler.compile(this.condition, BasicTypeID.BOOL);
		Statement onThen = this.onThen.compile(compiler);
		Statement onElse = this.onElse == null ? null : this.onElse.compile(compiler);
		return result(new IfStatement(position, condition, onThen, onElse), compiler);
	}
}
