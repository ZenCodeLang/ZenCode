/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeAssociative implements IParsedType {
	private final CodePosition position;
	public final IParsedType key;
	public final IParsedType value;
	public final int modifiers;
	private final ParsedStorageTag storage;
	
	public ParsedTypeAssociative(CodePosition position, IParsedType key, IParsedType value, ParsedStorageTag storage) {
		this.position = position;
		this.key = key;
		this.value = value;
		this.modifiers = 0;
		this.storage = storage;
	}

	private ParsedTypeAssociative(CodePosition position, IParsedType key, IParsedType value, int modifiers, ParsedStorageTag storage) {
		this.position = position;
		this.key = key;
		this.value = value;
		this.modifiers = modifiers;
		this.storage = storage;
	}
	
	@Override
	public IParsedType withOptional() {
		return new ParsedTypeAssociative(position, key, value, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL, storage);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeAssociative(position, key, value, this.modifiers | modifiers, storage);
	}

	@Override
	public ITypeID compile(TypeResolutionContext context) {
		StorageTag storage = this.storage.resolve(position, context);
		ITypeID key = this.key.compile(context);
		ITypeID value = this.value.compile(context);
		GlobalTypeRegistry registry = context.getTypeRegistry();
		return registry.getModified(modifiers, context.getTypeRegistry().getAssociative(key, value, storage));
	}
}
