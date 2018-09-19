/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFunctionType implements IParsedType {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedFunctionHeader header;
	private final ParsedStorageTag storageTag;
	
	public ParsedFunctionType(CodePosition position, ParsedFunctionHeader header, ParsedStorageTag storageTag) {
		this.position = position;
		this.header = header;
		this.modifiers = 0;
		this.storageTag = storageTag;
	}
	
	private ParsedFunctionType(CodePosition position, ParsedFunctionHeader header, int modifiers, ParsedStorageTag storageTag) {
		this.position = position;
		this.header = header;
		this.modifiers = modifiers;
		this.storageTag = storageTag;
	}
	
	@Override
	public IParsedType withOptional() {
		return new ParsedFunctionType(position, header, modifiers | ModifiedTypeID.MODIFIER_OPTIONAL, storageTag);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedFunctionType(position, header, modifiers | this.modifiers, storageTag);
	}

	@Override
	public ITypeID compile(TypeResolutionContext context) {
		return context.getTypeRegistry().getModified(modifiers, context.getTypeRegistry().getFunction(header.compile(context)));
	}
}
