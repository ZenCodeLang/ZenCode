/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedFunctionType implements IParsedType {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedFunctionHeader header;
	
	public ParsedFunctionType(CodePosition position, ParsedFunctionHeader header) {
		this.position = position;
		this.header = header;
		this.modifiers = 0;
	}
	
	private ParsedFunctionType(CodePosition position, ParsedFunctionHeader header, int modifiers) {
		this.position = position;
		this.header = header;
		this.modifiers = modifiers;
	}
	
	@Override
	public IParsedType withOptional() {
		return new ParsedFunctionType(position, header, modifiers | TypeMembers.MODIFIER_OPTIONAL);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedFunctionType(position, header, modifiers | this.modifiers);
	}

	@Override
	public ITypeID compile(BaseScope scope) {
		return scope.getTypeRegistry().getModified(modifiers, scope.getTypeRegistry().getFunction(header.compile(scope)));
	}
}
