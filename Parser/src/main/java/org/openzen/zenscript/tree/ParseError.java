package org.openzen.zenscript.tree;

import org.openzen.zencode.shared.CodePosition;

public class ParseError {

	private final CodePosition position;
	private final String message;

	public ParseError(CodePosition position, String message) {
		this.position = position;
		this.message = message;
	}

	public CodePosition position() {
		return position;
	}

	public String message() {
		return message;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("(");
		sb.append(position.fromLine).append(":").append(position.fromLineOffset).append("->").append(position.toLine).append(":").append(position.toLineOffset).append(")").append(message);
		return sb.toString();
	}
}
