/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.member.SetterMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class SetterMemberRef extends PropertyRef {
	public final SetterMember member;
	
	public SetterMemberRef(SetterMember member, GenericMapper mapper) {
		super(member, mapper);
		
		this.member = member;
	}

	@Override
	public SetterMemberRef getOverrides() {
		return member.getOverrides();
	}
}
