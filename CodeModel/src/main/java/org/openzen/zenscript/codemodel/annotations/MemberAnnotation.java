/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

/**
 *
 * @author Hoofdgebruiker
 */
public interface MemberAnnotation {
	public static final MemberAnnotation[] NONE = new MemberAnnotation[0];
	
	public AnnotationDefinition getDefinition();
	
	public void apply(IDefinitionMember member, BaseScope scope);
	
	public void applyOnOverridingMethod(FunctionalMember member, BaseScope scope);
	
	public void applyOnOverridingGetter(GetterMember member, BaseScope scope);
	
	public void applyOnOverridingSetter(SetterMember member, BaseScope scope);
	
	public void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeContext context);
}
