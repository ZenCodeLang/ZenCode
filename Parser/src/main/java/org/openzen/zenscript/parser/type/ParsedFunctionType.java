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
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFunctionType implements IParsedType {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedFunctionHeader header;
	private final ParsedStorageTag storage;
	
	public ParsedFunctionType(CodePosition position, ParsedFunctionHeader header, ParsedStorageTag storage) {
		this.position = position;
		this.header = header;
		this.modifiers = 0;
		this.storage = storage;
	}
	
	private ParsedFunctionType(CodePosition position, ParsedFunctionHeader header, int modifiers, ParsedStorageTag storageTag) {
		this.position = position;
		this.header = header;
		this.modifiers = modifiers;
		this.storage = storageTag;
	}
	
	@Override
	public IParsedType withOptional() {
		return new ParsedFunctionType(position, header, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL, storage);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedFunctionType(position, header, modifiers | this.modifiers, storage);
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		StorageTag storage = this.storage == null ? SharedStorageTag.INSTANCE : this.storage.resolve(position, context);
		return context.getTypeRegistry().getModified(modifiers, context.getTypeRegistry().getFunction(header.compile(context))).stored(storage);
	}
	
	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		if (storage != null)
			return new InvalidTypeID(position, CompileExceptionCode.STORAGE_NOT_SUPPORTED, "Storage tag not supported here");
		
		return context.getTypeRegistry().getModified(modifiers, context.getTypeRegistry().getFunction(header.compile(context)));
	}
}
