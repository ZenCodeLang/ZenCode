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
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFunctionType implements IParsedType {
	private final CodePosition position;
	private final ParsedFunctionHeader header;
	private final ParsedStorageTag storage;
	
	public ParsedFunctionType(CodePosition position, ParsedFunctionHeader header, ParsedStorageTag storage) {
		this.position = position;
		this.header = header;
		this.storage = storage;
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		StorageTag storage = this.storage.resolve(position, context);
		return context.getTypeRegistry().getFunction(header.compile(context)).stored(storage);
	}
	
	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		if (storage != ParsedStorageTag.NULL)
			return new InvalidTypeID(position, CompileExceptionCode.STORAGE_NOT_SUPPORTED, "Storage tag not supported here");
		
		return context.getTypeRegistry().getFunction(header.compile(context));
	}
}
