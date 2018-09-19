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

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeAssociative implements IParsedType {
	public final IParsedType key;
	public final IParsedType value;
	public final int modifiers;
	private final ParsedStorageTag storage;
	
	public ParsedTypeAssociative(IParsedType key, IParsedType value, ParsedStorageTag storage) {
		this.key = key;
		this.value = value;
		this.modifiers = 0;
		this.storage = storage;
	}

	private ParsedTypeAssociative(IParsedType key, IParsedType value, int modifiers, ParsedStorageTag storage) {
		this.key = key;
		this.value = value;
		this.modifiers = modifiers;
		this.storage = storage;
	}
	
	@Override
	public IParsedType withOptional() {
		return new ParsedTypeAssociative(key, value, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL, storage);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeAssociative(key, value, this.modifiers | modifiers, storage);
	}

	@Override
	public ITypeID compile(TypeResolutionContext context) {
		ITypeID key = this.key.compile(context);
		ITypeID value = this.value.compile(context);
		GlobalTypeRegistry registry = context.getTypeRegistry();
		return registry.getModified(modifiers, context.getTypeRegistry().getAssociative(key, value));
	}
}
