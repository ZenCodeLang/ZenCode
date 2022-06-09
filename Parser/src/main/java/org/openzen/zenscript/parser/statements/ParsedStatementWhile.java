package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.WhileStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementWhile extends ParsedStatement {
	public final CompilableExpression condition;
	public final ParsedStatement content;
	public final String label;

	public ParsedStatementWhile(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String label, CompilableExpression condition, ParsedStatement content) {
		super(position, annotations, whitespace);

		this.condition = condition;
		this.content = content;
		this.label = label;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		Expression condition = compiler.compile(this.condition, BasicTypeID.BOOL);
		WhileStatement result = new WhileStatement(position, label, condition);
		result.content = this.content.compile(compiler.forLoop(result));
		return result(result, compiler);
	}
}
