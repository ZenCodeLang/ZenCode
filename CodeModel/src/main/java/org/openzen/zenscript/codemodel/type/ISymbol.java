package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;

public interface ISymbol {
	IPartialExpression getExpression(CodePosition position, BaseScope scope, TypeID[] typeArguments);

	TypeID getType(CodePosition position, TypeResolutionContext context, TypeID[] typeArguments);
}
