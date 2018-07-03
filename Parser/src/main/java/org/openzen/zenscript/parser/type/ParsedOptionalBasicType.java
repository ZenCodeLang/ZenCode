/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.scope.BaseScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedOptionalBasicType implements IParsedType {
	private final ParsedTypeBasic type;
	
	public ParsedOptionalBasicType(ParsedTypeBasic type) {
		this.type = type;
	}

	@Override
	public IParsedType withOptional() {
		return this;
	}

	@Override
	public IParsedType withModifiers(int modifiers) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ITypeID compile(BaseScope scope) {
		return scope.getTypeRegistry().getModified(TypeMembers.MODIFIER_OPTIONAL, type.compile(scope));
	}
}
