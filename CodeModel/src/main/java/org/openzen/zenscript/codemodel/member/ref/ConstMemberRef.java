/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.member.ConstMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstMemberRef extends PropertyRef {
	public final ConstMember member;
	
	public ConstMemberRef(ConstMember member, GenericMapper mapper) {
		super(member, mapper);
		this.member = member;
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}
}
