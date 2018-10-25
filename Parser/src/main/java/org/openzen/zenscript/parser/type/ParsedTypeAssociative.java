/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeAssociative implements IParsedType {
	private final CodePosition position;
	public final IParsedType key;
	public final IParsedType value;
	private final ParsedStorageTag storage;
	
	public ParsedTypeAssociative(CodePosition position, IParsedType key, IParsedType value, ParsedStorageTag storage) {
		this.position = position;
		this.key = key;
		this.value = value;
		this.storage = storage;
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		StorageTag storage = this.storage.resolve(position, context);
		StoredType key = this.key.compile(context);
		StoredType value = this.value.compile(context);
		return context.getTypeRegistry().getAssociative(key, value).stored(storage);
	}

	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		if (storage != ParsedStorageTag.NULL)
			return new InvalidTypeID(position, CompileExceptionCode.STORAGE_NOT_SUPPORTED, "Storage tag not supported here");
		
		StorageTag storage = this.storage.resolve(position, context);
		StoredType key = this.key.compile(context);
		StoredType value = this.value.compile(context);
		return context.getTypeRegistry().getAssociative(key, value);
	}
}
