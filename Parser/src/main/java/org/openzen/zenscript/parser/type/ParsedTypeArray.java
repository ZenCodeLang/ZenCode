/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeArray implements IParsedType {
	public final IParsedType baseType;
	public final int dimension;
	public final int modifiers;
	
	public ParsedTypeArray(IParsedType baseType, int dimension) {
		this.baseType = baseType;
		this.dimension = dimension;
		this.modifiers = 0;
	}

	private ParsedTypeArray(IParsedType baseType, int dimension, int modifiers) {
		this.baseType = baseType;
		this.dimension = dimension;
		this.modifiers = modifiers;
	}
	
	@Override
	public IParsedType withOptional() {
		return new ParsedTypeArray(baseType, dimension, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeArray(baseType, dimension, this.modifiers | modifiers);
	}

	@Override
	public ITypeID compile(TypeResolutionContext context) {
		ITypeID baseType = this.baseType.compile(context);
		GlobalTypeRegistry registry = context.getTypeRegistry();
		return registry.getModified(modifiers, registry.getArray(baseType, dimension));
	}
}
