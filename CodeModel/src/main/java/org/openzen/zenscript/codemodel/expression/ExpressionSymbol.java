package org.openzen.zenscript.codemodel.expression;

import java.util.function.BiFunction;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ExpressionSymbol implements ISymbol {
	private final BiFunction<CodePosition, BaseScope, IPartialExpression> function;
	
	public ExpressionSymbol(BiFunction<CodePosition, BaseScope, IPartialExpression> function) {
		this.function = function;
	}

	@Override
	public IPartialExpression getExpression(CodePosition position, BaseScope scope, TypeID[] typeArguments) {
		return function.apply(position, scope);
	}

	@Override
	public TypeID getType(CodePosition position, TypeResolutionContext context, TypeID[] typeArguments) {
		return null;
	}
}
