package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class MatchNode extends ExpressionNode {
	private final CodePosition position;
	private final ExpressionNode value;
	private final List<CaseNode> cases;

	public MatchNode(CodePosition position, ExpressionNode value, List<CaseNode> cases) {
		this.position = position;
		this.value = value;
		this.cases = cases;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitMatch(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public ExpressionNode value() {
		return value;
	}

	public List<CaseNode> cases() {
		return cases;
	}

	public static class CaseNode implements ASTNode {
		private final ExpressionNode name;
		private final ExpressionNode value;

		public CaseNode(ExpressionNode name, ExpressionNode value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
			return visitor.visitMatchCase(this, context);
		}

		@Override
		public CodePosition position() {
			return null;
		}

		public ExpressionNode name() {
			return name;
		}

		public ExpressionNode value() {
			return value;
		}
	}
}
