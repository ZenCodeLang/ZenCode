/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeArray implements IParsedType {
	private final CodePosition position;
	public final IParsedType baseType;
	public final int dimension;
	public final ParsedStorageTag storage;
	
	public ParsedTypeArray(CodePosition position, IParsedType baseType, int dimension, ParsedStorageTag storage) {
		if (storage == null)
			throw new NullPointerException();
		
		this.position = position;
		this.baseType = baseType;
		this.dimension = dimension;
		this.storage = storage;
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		StoredType baseType = this.baseType.compile(context);
		StorageTag storage = this.storage.resolve(position, context);
		GlobalTypeRegistry registry = context.getTypeRegistry();
		return registry.getArray(baseType, dimension).stored(storage);
	}
	
	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		if (storage != ParsedStorageTag.NULL)
			return new InvalidTypeID(position, CompileExceptionCode.STORAGE_NOT_SUPPORTED, "Storage tag not supported here");
		
		StoredType baseType = this.baseType.compile(context);
		StorageTag storage = this.storage.resolve(position, context);
		GlobalTypeRegistry registry = context.getTypeRegistry();
		return registry.getArray(baseType, dimension);
	}
}
