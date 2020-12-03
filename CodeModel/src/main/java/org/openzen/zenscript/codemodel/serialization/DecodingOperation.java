package org.openzen.zenscript.codemodel.serialization;

public interface DecodingOperation {
	void decode(CodeSerializationInput input) throws DeserializationException;
}
