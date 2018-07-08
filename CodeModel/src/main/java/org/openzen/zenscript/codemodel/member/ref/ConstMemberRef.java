/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstMemberRef implements DefinitionMemberRef {
	public final ConstMember member;
	public final ITypeID type;
	
	public ConstMemberRef(ConstMember member, ITypeID type) {
		this.member = member;
		this.type = type;
	}

	@Override
	public CodePosition getPosition() {
		return member.position;
	}

	@Override
	public String describe() {
		return member.describe();
	}

	@Override
	public <T> T getTag(Class<T> type) {
		return member.getTag(type);
	}
}
