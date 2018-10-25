/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParameterTypeBound extends TypeParameterBound {
	public final CodePosition position;
	public final TypeID type;
	
	public ParameterTypeBound(CodePosition position, TypeID type) {
		this.position = position;
		this.type = type;
	}
	
	@Override
	public String getCanonical() {
		return type.toString();
	}

	@Override
	public void registerMembers(LocalMemberCache cache, TypeMembers members) {
		cache.get(this.type.stored(members.type.getSpecifiedStorage()))
				.copyMembersTo(position, members, TypeMemberPriority.FROM_TYPE_BOUNDS);
	}

	@Override
	public boolean matches(LocalMemberCache cache, TypeID type) {
		return cache.get(type.stored(BorrowStorageTag.THIS))
				.extendsOrImplements(this.type);
	}

	@Override
	public TypeParameterBound instance(GenericMapper mapper) {
		return new ParameterTypeBound(position, type.instance(mapper, null).type);
	}

	@Override
	public <T> T accept(GenericParameterBoundVisitor<T> visitor) {
		return visitor.visitType(this);
	}

	@Override
	public <C, R> R accept(C context, GenericParameterBoundVisitorWithContext<C, R> visitor) {
		return visitor.visitType(context, this);
	}

	@Override
	public boolean isObjectType() {
		return true;
	}
}
