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
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;

/**
 *
 * @author Hoofdgebruiker
 */
public interface CompilingType {
	CompilingType getInner(String name);
	
	HighLevelDefinition load();
	
	default DefinitionTypeID getInnerType(GlobalTypeRegistry registry, List<GenericName> name, int index, DefinitionTypeID outer) {
		DefinitionTypeID type = registry.getForDefinition(load(), name.get(index).arguments, outer, null);
		index++;
		if (index == name.size())
			return type;
		
		CompilingType innerType = getInner(name.get(index).name);
		if (innerType == null)
			return null;
		
		return innerType.getInnerType(registry, name, index, type);
	}
}
