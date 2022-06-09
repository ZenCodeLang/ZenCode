package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;

import java.util.Optional;

public interface ISymbol {
	CompilableExpression getExpression(CodePosition position, TypeID[] typeArguments);

	Optional<TypeID> getType(CodePosition position, TypeBuilder types, TypeID[] typeArguments);
}
