/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.List;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericName {
	public final String name;
	public final TypeID[] arguments;
	
	public GenericName(String name) {
		this(name, TypeID.NONE);
	}
	
	public GenericName(String name, TypeID[] arguments) {
		if (arguments == null)
			throw new NullPointerException("Arguments cannot be null");
		
		this.name = name;
		this.arguments = arguments;
	}
	
	public int getNumberOfArguments() {
		return arguments.length;
	}
	
	public boolean hasArguments() {
		return arguments.length > 0;
	}
	
	public boolean hasNoArguments() {
		return arguments.length == 0;
	}
	
	public static TypeID getInnerType(GlobalTypeRegistry registry, DefinitionTypeID type, List<GenericName> name, int index) {
		while (index < name.size()) {
			GenericName innerName = name.get(index++);
			type = type.getInnerType(innerName, registry);
			if (type == null)
				return null;
		}

		return type;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(name);
		if (hasArguments()) {
			result.append("<");
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0)
					result.append(", ");
				result.append(arguments[i].toString());
			}
			result.append(">");
		}
		return result.toString();
	}
}
