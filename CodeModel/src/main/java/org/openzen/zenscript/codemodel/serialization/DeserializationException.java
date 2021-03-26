package org.openzen.zenscript.codemodel.serialization;

public class DeserializationException extends Exception {
	public DeserializationException(String reason) {
		super(reason);
	}

	public DeserializationException(String reason, Exception cause) {
		super (reason, cause);
	}
}
