package org.openzen.zenscript.lexer;

import org.openzen.zencode.shared.CodePosition;

public class ParseException extends Exception {
	public final CodePosition position;
	public final String message;

	public ParseException(CodePosition position, String message) {
		super(position.toString() + ": " + message);

		this.position = position;
		this.message = message;
	}

	public ParseException(CodePosition position, String message, Throwable cause) {
		super(position + ": " + message, cause);

		this.position = position;
		this.message = message;
	}
}
