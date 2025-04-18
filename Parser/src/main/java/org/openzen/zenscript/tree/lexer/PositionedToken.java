package org.openzen.zenscript.tree.lexer;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.Token;
import org.openzen.zenscript.lexer.TokenType;

public class PositionedToken<TT extends TokenType, T extends Token<TT>> implements Token<TT> {
	private final CodePosition position;
	private final T token;

	public PositionedToken(CodePosition position, T token) {
		this.position = position;
		this.token = token;
	}

	public CodePosition position() {
		return position;
	}

	public T token() {
		return token;
	}

	@Override
	public TT getType() {
		return token.getType();
	}

	@Override
	public String getContent() {
		return token.getContent();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("(");
		sb.append(position.fromLine).append(":").append(position.fromLineOffset).append("->").append(position.toLine).append(":").append(position.toLineOffset).append(")").append(token);
		return sb.toString();
	}
}
