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
public interface MemberAnnotation {
	public static final MemberAnnotation[] NONE = new MemberAnnotation[0];
	
	public void apply(IDefinitionMember member, BaseScope scope);
	
	public void applyOnOverridingMethod(FunctionalMember member, BaseScope scope);
}
