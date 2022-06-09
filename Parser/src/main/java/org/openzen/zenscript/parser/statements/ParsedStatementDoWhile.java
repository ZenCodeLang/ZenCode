package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.DoWhileStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;

public class ParsedStatementDoWhile extends ParsedStatement {
	public final String label;
	public final ParsedStatement content;
	public final CompilableExpression condition;

	public ParsedStatementDoWhile(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String label, ParsedStatement content, CompilableExpression condition) {
		super(position, annotations, whitespace);

		this.label = label;
		this.content = content;
		this.condition = condition;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		Expression condition = compiler.compile(this.condition, BasicTypeID.BOOL);
		DoWhileStatement result = new DoWhileStatement(position, label, condition);
		StatementCompiler innerScope = compiler.forLoop(result);
		result.content = this.content.compile(innerScope);
		return result(result, compiler);
	}
}
