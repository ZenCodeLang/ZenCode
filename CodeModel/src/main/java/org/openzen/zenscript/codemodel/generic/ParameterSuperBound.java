/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

import java.util.Map;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParameterSuperBound extends GenericParameterBound {
	public final ITypeID type;
	
	public ParameterSuperBound(ITypeID type) {
		this.type = type;
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
	public GenericParameterBound withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		ITypeID translated = type.withGenericArguments(registry, arguments);
		if (translated == type)
			return this;
		
		return new ParameterSuperBound(translated);
	}

	@Override
	public <T> T accept(GenericParameterBoundVisitor<T> visitor) {
		return visitor.visitSuper(this);
	}

	@Override
	public boolean isObjectType() {
		return true;
	}
}
