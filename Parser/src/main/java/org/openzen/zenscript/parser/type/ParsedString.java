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
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedString implements IParsedType {
	private final CodePosition position;
	private final ParsedStorageTag storage;
	
	public ParsedString(CodePosition position, ParsedStorageTag storage) {
		this.position = position;
		this.storage = storage;
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		return StringTypeID.INSTANCE.stored(storage.resolve(position, context));
	}

	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		if (storage != ParsedStorageTag.NULL)
			return new InvalidTypeID(position, CompileExceptionCode.STORAGE_NOT_SUPPORTED, "Storage tag not supported here");
		
		return StringTypeID.INSTANCE;
	}
}
