package org.openzen.zenscript.codemodel.context;

import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public interface CompilingType {
	CompilingType getInner(String name);

	TypeSymbol load();

	default TypeID getInnerType(List<GenericName> name, int index, TypeID outer) {
		TypeID type = DefinitionTypeID.create(load(), name.get(index).arguments);
		index++;
		if (index == name.size())
			return type;

		CompilingType innerType = getInner(name.get(index).name);
		if (innerType == null)
			return null;

		return innerType.getInnerType(name, index, type);
	}
}
