/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class NativeMemberAnnotation implements MemberAnnotation {
	private final String identifier;
	
	public NativeMemberAnnotation(String identifier) {
		this.identifier = identifier;
	}
	
	@Override
	public void apply(IDefinitionMember member, BaseScope scope) {
		member.setTag(NativeTag.class, new NativeTag(identifier));
	}

	@Override
	public void applyOnOverridingMethod(FunctionalMember member, BaseScope scope) {
		// not inherited
	}
}
