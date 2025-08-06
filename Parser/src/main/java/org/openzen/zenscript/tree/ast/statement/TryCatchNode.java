package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.misc.NameNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class TryCatchNode extends StatementNode {
	private final CodePosition position;
	private final NameNode resourceName;
	private final ExpressionNode resourceInitializer;
	private final ASTNode body;
	private final List<CatchClauseNode> catchClauses;
	private final ASTNode finallyClause;

	public TryCatchNode(CodePosition position, NameNode resourceName, ExpressionNode resourceInitializer, ASTNode body, List<CatchClauseNode> catchClauses, ASTNode finallyClause) {
		this.position = position;
		this.resourceName = resourceName;
		this.resourceInitializer = resourceInitializer;
		this.body = body;
		this.catchClauses = catchClauses;
		this.finallyClause = finallyClause;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitTryCatch(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public NameNode resourceName() {
		return resourceName;
	}

	public ExpressionNode resourceInitializer() {
		return resourceInitializer;
	}

	public ASTNode body() {
		return body;
	}

	public List<CatchClauseNode> catchClauses() {
		return catchClauses;
	}

	public ASTNode finallyClause() {
		return finallyClause;
	}

	public static class CatchClauseNode implements ASTNode {
		private final NameNode exceptionName;
		private final ASTNode body;

		public CatchClauseNode(NameNode exceptionName, ASTNode body) {
			this.exceptionName = exceptionName;
			this.body = body;
		}

		@Override
		public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
			return visitor.visitTryCatchClause(this, context);
		}

		@Override
		public CodePosition position() {
			return null;
		}

		public NameNode exceptionName() {
			return exceptionName;
		}

		public ASTNode body() {
			return body;
		}
	}

}
