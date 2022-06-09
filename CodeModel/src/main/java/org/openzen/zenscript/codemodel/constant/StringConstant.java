package org.openzen.zenscript.codemodel.constant;

import java.util.Optional;

public class StringConstant implements CompileTimeConstant {
	public final String value;

	public StringConstant(String value) {
		this.value = value;
	}

	@Override
	public Optional<StringConstant> asString() {
		return Optional.of(this);
	}
}
