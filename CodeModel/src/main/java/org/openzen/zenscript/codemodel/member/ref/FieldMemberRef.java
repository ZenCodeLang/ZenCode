/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.member.FieldMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class FieldMemberRef extends PropertyRef {
	public final FieldMember member;
	
	public FieldMemberRef(FieldMember member, GenericMapper mapper) {
		super(member, mapper);
		this.member = member;
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}
}
