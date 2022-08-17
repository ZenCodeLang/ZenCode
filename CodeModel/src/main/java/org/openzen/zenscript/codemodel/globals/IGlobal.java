package org.openzen.zenscript.codemodel.globals;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public interface IGlobal {
	CompilableExpression getExpression(CodePosition position, TypeID[] typeArguments);

	Optional<TypeID> getType(CodePosition position, TypeBuilder types, TypeID[] typeArguments);
}
