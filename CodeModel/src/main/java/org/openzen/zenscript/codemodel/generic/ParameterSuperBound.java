/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParameterSuperBound extends TypeParameterBound {
	public final ITypeID type;
	
	public ParameterSuperBound(ITypeID type) {
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
	public boolean matches(LocalMemberCache cache, ITypeID type) {
		return cache.get(this.type).extendsOrImplements(type);
	}

	@Override
	public TypeParameterBound instance(GenericMapper mapper) {
		ITypeID translated = type.instance(mapper);
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
