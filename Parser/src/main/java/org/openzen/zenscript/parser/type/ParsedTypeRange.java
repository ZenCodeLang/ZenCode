/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.linker.BaseScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeRange implements IParsedType {
	private final IParsedType from;
	private final IParsedType to;
	private final int modifiers;
	
	public ParsedTypeRange(IParsedType from, IParsedType to) {
		this.from = from;
		this.to = to;
		this.modifiers = 0;
	}
	
	private ParsedTypeRange(IParsedType from, IParsedType to, int modifiers) {
		this.from = from;
		this.to = to;
		this.modifiers = modifiers;
	}

	@Override
	public IParsedType withOptional() {
		return new ParsedTypeRange(from, to, modifiers | TypeMembers.MODIFIER_OPTIONAL);
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		return new ParsedTypeRange(from, to, this.modifiers | modifiers);
	}
	
	@Override
	public ITypeID compile(BaseScope scope) {
		ITypeID from = this.from.compile(scope);
		ITypeID to = this.to.compile(scope);
		return scope.getTypeRegistry().getModified(modifiers, scope.getTypeRegistry().getRange(from, to));
	}
}
