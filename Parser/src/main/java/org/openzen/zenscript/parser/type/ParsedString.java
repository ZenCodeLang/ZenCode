/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.StringTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedString implements IParsedType {
	private final int modifiers;
	private final ParsedStorageTag storageTag;
	
	public ParsedString(int modifiers, ParsedStorageTag storageTag) {
		this.modifiers = modifiers;
		this.storageTag = storageTag;
	}

	@Override
	public IParsedType withOptional() {
		return new ParsedString(modifiers | ModifiedTypeID.MODIFIER_OPTIONAL, storageTag);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedString(this.modifiers | modifiers, storageTag);
	}

	@Override
	public ITypeID compile(TypeResolutionContext context) {
		return StringTypeID.UNIQUE; // TODO
	}
}
