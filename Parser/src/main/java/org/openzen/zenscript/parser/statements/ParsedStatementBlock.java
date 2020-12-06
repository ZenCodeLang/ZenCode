package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.WhitespacePostComment;
import org.openzen.zenscript.codemodel.scope.BlockScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.List;

public class ParsedStatementBlock extends ParsedStatement {
	private final List<ParsedStatement> statements;
	private final WhitespacePostComment postComment;

	public ParsedStatementBlock(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, WhitespacePostComment postComment, List<ParsedStatement> statements) {
		super(position, annotations, whitespace);

		this.statements = statements;
		this.postComment = postComment;
	}

	@Override
	public Statement compile(StatementScope scope) {
		StatementScope blockScope = new BlockScope(scope);
		Statement[] compiled = new Statement[statements.size()];
		int i = 0;
		for (ParsedStatement statement : statements) {
			compiled[i++] = statement.compile(blockScope);
		}
		BlockStatement block = new BlockStatement(position, compiled);
		result(block, scope);
		block.setTag(WhitespacePostComment.class, postComment);
		return block;
	}
}
