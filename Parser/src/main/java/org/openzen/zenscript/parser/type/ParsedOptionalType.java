/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
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
	public TypeID compile(TypeResolutionContext context) {
		TypeID base = type.compile(context);
		return context.getTypeRegistry().getOptional(base);
	}
}
