package org.openzen.zenscript.tree.ast.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.tree.ast.ASTNode;
import org.openzen.zenscript.tree.ast.expression.ExpressionNode;
import org.openzen.zenscript.tree.ast.misc.LabelNode;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.List;

public class SwitchNode  extends StatementNode{
	private final CodePosition position;
	private final LabelNode label;
	private final ExpressionNode expression;
	private final SwitchCasesNode cases;

	public SwitchNode(CodePosition position, LabelNode label, ExpressionNode expression, SwitchCasesNode cases) {
		this.position = position;
		this.label = label;
		this.expression = expression;
		this.cases = cases;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitSwitch(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public LabelNode label() {
		return label;
	}

	public ExpressionNode expression() {
		return expression;
	}

	public SwitchCasesNode cases() {
		return cases;
	}

	public static class SwitchCasesNode implements ASTNode {
		private final List<SwitchCaseNode> cases;

		public SwitchCasesNode(List<SwitchCaseNode> cases) {
			this.cases = cases;
		}

		@Override
		public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
			return visitor.visitSwitchCases(this, context);
		}

		@Override
		public CodePosition position() {
			return null;
		}

		public List<SwitchCaseNode> cases() {
			return cases;
		}
	}

	public static class SwitchCaseNode implements ASTNode {

		private final ExpressionNode expression;
		private final List<ASTNode> statements;

		public SwitchCaseNode(ExpressionNode expression, List<ASTNode> statements) {
			this.expression = expression;
			this.statements = statements;
		}

		@Override
		public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
			return visitor.visitSwitchCase(this, context);
		}

		@Override
		public CodePosition position() {
			return null;
		}

		public ExpressionNode expression() {
			return expression;
		}

		public List<ASTNode> statements() {
			return statements;
		}
	}
}
