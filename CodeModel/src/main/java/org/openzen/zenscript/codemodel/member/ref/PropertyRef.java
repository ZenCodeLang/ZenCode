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
import org.openzen.zenscript.codemodel.member.IPropertyMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class PropertyRef implements DefinitionMemberRef {
	private final IPropertyMember member;
	
	private ITypeID type;
	private GenericMapper mapper;
	
	public PropertyRef(IPropertyMember member, GenericMapper mapper) {
		this.member = member;
		
		if (member.getType() == BasicTypeID.UNDETERMINED) {
			type = null;
			this.mapper = mapper;
		} else {
			type = member.getType().instance(mapper);
			this.mapper = null;
		}
	}
	
	public final ITypeID getType() {
		if (type == null) {
			if (member.getType() == BasicTypeID.UNDETERMINED)
				throw new IllegalStateException("Property is not yet resolved!");
			
			type = member.getType().instance(mapper);
			mapper = null;
		}
		
		return type;
	}

	@Override
	public final CodePosition getPosition() {
		return member.getPosition();
	}

	@Override
	public final String describe() {
		return member.describe();
	}

	@Override
	public final <T> T getTag(Class<T> type) {
		return member.getTag(type);
	}
	
	public final boolean isStatic() {
		return member.isStatic();
	}
	
	public final boolean isFinal() {
		return member.isFinal();
	}

	@Override
	public final FunctionHeader getHeader() {
		return null;
	}

	@Override
	public final MemberAnnotation[] getAnnotations() {
		return member.getAnnotations();
	}
}
