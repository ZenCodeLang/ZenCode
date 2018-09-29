/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ImplementationMemberRef implements DefinitionMemberRef {
	public final ImplementationMember member;
	private final StoredType type;
	public final TypeID implementsType;
	
	public ImplementationMemberRef(ImplementationMember member, StoredType owner, TypeID implementsType) {
		this.member = member;
		this.type = owner;
		this.implementsType = implementsType;
	}

	@Override
	public CodePosition getPosition() {
		return member.position;
	}
	
	@Override
	public StoredType getOwnerType() {
		return type;
	}

	@Override
	public String describe() {
		return member.describe();
	}

	@Override
	public <T> T getTag(Class<T> type) {
		return member.getTag(type);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public FunctionHeader getHeader() {
		return null;
	}

	@Override
	public MemberAnnotation[] getAnnotations() {
		return member.annotations;
	}

	@Override
	public IDefinitionMember getTarget() {
		return member;
	}
}
