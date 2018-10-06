/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedOptionalType implements IParsedType {
	private final IParsedType type;
	
	public ParsedOptionalType(IParsedType type) {
		this.type = type;
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		StoredType base = type.compile(context);
		return new StoredType(context.getTypeRegistry().getOptional(base.type), base.getSpecifiedStorage());
	}

	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		return context.getTypeRegistry().getOptional(type.compileUnstored(context));
	}
}
