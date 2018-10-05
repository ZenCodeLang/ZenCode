/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeSymbol implements ISymbol {
	private final HighLevelDefinition definition;
	
	public TypeSymbol(HighLevelDefinition definition) {
		this.definition = definition;
	}
	
	@Override
	public IPartialExpression getExpression(CodePosition position, BaseScope scope, TypeArgument[] typeArguments) {
		return new PartialTypeExpression(position, scope.getTypeRegistry().getForDefinition(definition, typeArguments), typeArguments);
	}

	@Override
	public TypeID getType(CodePosition position, TypeResolutionContext context, TypeArgument[] typeArguments) {
		return context.getTypeRegistry().getForDefinition(definition, typeArguments);
	}
}
