/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedVariantOption {
	public final String name;
	public final List<IParsedType> types;
	
	public ParsedVariantOption(String name, List<IParsedType> types) {
		this.name = name;
		this.types = types;
	}
	
	public VariantDefinition.Option compile(BaseScope scope) {
		ITypeID[] cTypes = new ITypeID[types.size()];
		for (int i = 0; i < cTypes.length; i++)
			cTypes[i] = types.get(i).compile(scope);
		
		return new VariantDefinition.Option(name, cTypes);
	}
}
