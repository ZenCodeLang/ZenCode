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
public class ParsedTypeAssociative implements IParsedType {
	public final IParsedType key;
	public final IParsedType value;

	public ParsedTypeAssociative(IParsedType key, IParsedType value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public TypeID compile(TypeResolutionContext context) {
		TypeID key = this.key.compile(context);
		TypeID value = this.value.compile(context);
		return context.getTypeRegistry().getAssociative(key, value);
	}
}
