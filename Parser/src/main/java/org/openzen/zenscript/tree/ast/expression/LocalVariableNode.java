package org.openzen.zenscript.tree.ast.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;
import org.openzen.zenscript.tree.lexer.PositionedToken;

public class LocalVariableNode extends ExpressionNode {
	private final CodePosition position;
	private final PositionedToken<ZSTokenType, ZSToken> name;

	public LocalVariableNode(CodePosition position, PositionedToken<ZSTokenType, ZSToken> name) {
		this.position = position;
		this.name = name;
	}

	@Override
	public <C, R> R accept(ASTVisitor<C, R> visitor, C context) {
		return visitor.visitLocalVariable(this, context);
	}

	@Override
	public CodePosition position() {
		return position;
	}

	public PositionedToken<ZSTokenType, ZSToken> name() {
		return name;
	}
}
