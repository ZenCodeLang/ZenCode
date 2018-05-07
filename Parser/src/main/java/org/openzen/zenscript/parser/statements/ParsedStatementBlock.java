package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.WhitespacePostComment;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.linker.BlockScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.shared.CodePosition;

public class ParsedStatementBlock extends ParsedStatement {
	private final List<ParsedStatement> statements;
	private final WhitespacePostComment postComment;
	
	public ParsedStatementBlock(CodePosition position, WhitespaceInfo whitespace, WhitespacePostComment postComment, List<ParsedStatement> statements) {
		super(position, whitespace);

		this.statements = statements;
		this.postComment = postComment;
	}

	@Override
	public Statement compile(StatementScope scope) {
		StatementScope blockScope = new BlockScope(scope);
		List<Statement> compiled = new ArrayList<>();
		for (ParsedStatement statement : statements) {
			compiled.add(statement.compile(blockScope));
		}
		BlockStatement block = new BlockStatement(position, compiled);
		result(block);
		block.setTag(WhitespacePostComment.class, postComment);
		return block;
	}
}
