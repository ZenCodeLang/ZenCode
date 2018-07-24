/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstMemberRef implements DefinitionMemberRef {
	public final ConstMember member;
	private ITypeID type;
	private ITypeID originalType;
	private final GenericMapper mapper;
	
	public ConstMemberRef(ConstMember member, ITypeID originalType, GenericMapper mapper) {
		this.member = member;
		this.type = originalType.instance(mapper);
		this.originalType = originalType;
		this.mapper = mapper;
	}
	
	public ITypeID getType() {
		if (originalType != member.type) {
			originalType = member.type;
			type = originalType.instance(mapper);
		}
		return type;
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
}
