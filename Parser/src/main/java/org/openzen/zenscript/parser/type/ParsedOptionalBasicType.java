/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedOptionalBasicType implements IParsedType {
	private final ParsedTypeBasic type;
	
	public ParsedOptionalBasicType(ParsedTypeBasic type) {
		this.type = type;
	}

	@Override
	public IParsedType withOptional() {
		return this;
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		StoredType base = type.compile(context);
		return context.getTypeRegistry().getModified(ModifiedTypeID.MODIFIER_OPTIONAL, base.type).stored(base.storage);
	}

	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		return context.getTypeRegistry().getModified(ModifiedTypeID.MODIFIER_OPTIONAL, type.compileUnstored(context));
	}
}
