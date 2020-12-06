package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;

public class TypeSymbol implements ISymbol {
	private final HighLevelDefinition definition;

	public TypeSymbol(HighLevelDefinition definition) {
		this.definition = definition;
	}

	@Override
	public IPartialExpression getExpression(CodePosition position, BaseScope scope, TypeID[] typeArguments) {
		return new PartialTypeExpression(position, scope.getTypeRegistry().getForDefinition(definition, typeArguments), typeArguments);
	}

	@Override
	public TypeID getType(CodePosition position, TypeResolutionContext context, TypeID[] typeArguments) {
		return context.getTypeRegistry().getForDefinition(definition, typeArguments);
	}
}
