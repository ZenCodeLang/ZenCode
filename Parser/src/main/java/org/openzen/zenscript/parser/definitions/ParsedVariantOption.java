/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.List;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedVariantOption {
	public final String name;
	public final int ordinal;
	public final List<IParsedType> types;
	
	public ParsedVariantOption(String name, int ordinal, List<IParsedType> types) {
		this.name = name;
		this.ordinal = ordinal;
		this.types = types;
	}
	
	public VariantDefinition.Option compile(TypeResolutionContext context) {
		ITypeID[] cTypes = new ITypeID[types.size()];
		for (int i = 0; i < cTypes.length; i++)
			cTypes[i] = types.get(i).compile(context);
		
		return new VariantDefinition.Option(name, ordinal, cTypes);
	}
}
