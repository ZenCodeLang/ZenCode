/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.linker.symbol;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

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
	public IPartialExpression getExpression(CodePosition position, GlobalTypeRegistry types, List<ITypeID> typeArguments) {
		return new PartialTypeExpression(position, types.getForDefinition(definition, typeArguments));
	}

	@Override
	public ITypeID getType(CodePosition position, GlobalTypeRegistry types, List<ITypeID> typeArguments) {
		return types.getForDefinition(definition, typeArguments);
	}
}
