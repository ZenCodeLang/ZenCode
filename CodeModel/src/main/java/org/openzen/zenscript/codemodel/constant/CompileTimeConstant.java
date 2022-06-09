package org.openzen.zenscript.codemodel.constant;

import java.util.Optional;

public interface CompileTimeConstant {
	default Optional<StringConstant> asString() {
		return Optional.empty();
	}

	default Optional<EnumValueConstant> asEnumValue() {
		return Optional.empty();
	}
}
