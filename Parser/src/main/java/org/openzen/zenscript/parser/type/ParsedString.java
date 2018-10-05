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
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeArgument;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.AutoStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedString implements IParsedType {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedStorageTag storage;
	
	public ParsedString(CodePosition position, int modifiers, ParsedStorageTag storage) {
		this.position = position;
		this.modifiers = modifiers;
		this.storage = storage;
	}

	@Override
	public IParsedType withOptional() {
		return new ParsedString(position, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL, storage);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedString(position, this.modifiers | modifiers, storage);
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		return context.getTypeRegistry()
				.getModified(modifiers, StringTypeID.INSTANCE)
				.stored(storage == ParsedStorageTag.NULL ? AutoStorageTag.INSTANCE : storage.resolve(position, context));
	}

	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		if (storage != ParsedStorageTag.NULL)
			return new InvalidTypeID(position, CompileExceptionCode.STORAGE_NOT_SUPPORTED, "Storage tag not supported here");
		
		return StringTypeID.INSTANCE;
	}

	@Override
	public TypeArgument compileArgument(TypeResolutionContext context) {
		return new TypeArgument(context.getTypeRegistry()
				.getModified(modifiers, StringTypeID.INSTANCE), storage.resolve(position, context));
	}
}
