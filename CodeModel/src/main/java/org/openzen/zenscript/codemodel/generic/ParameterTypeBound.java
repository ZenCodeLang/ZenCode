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
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParameterTypeBound extends GenericParameterBound {
	public final CodePosition position;
	public final ITypeID type;
	
	public ParameterTypeBound(CodePosition position, ITypeID type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public void registerMembers(LocalMemberCache cache, TypeMembers type) {
		cache.get(this.type).copyMembersTo(position, type, TypeMemberPriority.FROM_TYPE_BOUNDS);
	}

	@Override
	public boolean matches(LocalMemberCache cache, ITypeID type) {
		return cache.get(type).extendsOrImplements(this.type);
	}

	@Override
	public GenericParameterBound withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		return new ParameterTypeBound(position, type.withGenericArguments(registry, arguments));
	}

	@Override
	public <T> T accept(GenericParameterBoundVisitor<T> visitor) {
		return visitor.visitType(this);
	}

	@Override
	public boolean isObjectType() {
		return true;
	}
}
