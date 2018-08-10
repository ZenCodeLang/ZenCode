/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GenericName;

/**
 *
 * @author Hoofdgebruiker
 */
public interface CompilingType {
	CompilingType getInner(String name);
	
	HighLevelDefinition load(TypeResolutionContext context);
	
	default DefinitionTypeID getInnerType(TypeResolutionContext context, List<GenericName> name, int index, DefinitionTypeID outer) {
		DefinitionTypeID type = context.getTypeRegistry().getForDefinition(load(context), name.get(index).arguments, outer);
		index++;
		if (index == name.size())
			return type;
		
		CompilingType innerType = getInner(name.get(index).name);
		if (innerType == null)
			return null;
		
		return innerType.getInnerType(context, name, index, type);
	}
}
