/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public final class ParameterSuperBound implements TypeParameterBound {
	public final TypeID type;
	
	public ParameterSuperBound(TypeID type) {
		this.type = type;
	}
	
	@Override
	public String getCanonical() {
		return "super|" + type.toString();
	}

	@Override
	public void registerMembers(LocalMemberCache cache, TypeMembers type) {
		// TODO: nothing?
	}

	@Override
	public boolean matches(LocalMemberCache cache, TypeID type) {
		return cache.get(this.type.stored(BorrowStorageTag.THIS)).extendsOrImplements(type);
	}

	@Override
	public TypeParameterBound instance(GenericMapper mapper) {
		TypeID translated = type.instance(mapper, null).type;
		if (translated == type)
			return this;
		
		return new ParameterSuperBound(translated);
	}

	@Override
	public <T> T accept(GenericParameterBoundVisitor<T> visitor) {
		return visitor.visitSuper(this);
	}

	@Override
	public <C, R> R accept(C context, GenericParameterBoundVisitorWithContext<C, R> visitor) {
		return visitor.visitSuper(context, this);
	}

	@Override
	public boolean isObjectType() {
		return true;
	}
}
