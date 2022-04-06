package org.openzen.zenscript.compiler.types;

import java.util.Optional;

public interface MemberGroup {
	boolean isCallable();

	Optional<Getter> getGetter();

	Optional<Setter> getSetter();
}
