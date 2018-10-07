/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDefinitionMember {
	public CodePosition getPosition();
	
	public int getSpecifiedModifiers();
	
	public int getEffectiveModifiers();
	
	public MemberAnnotation[] getAnnotations();
	
	public HighLevelDefinition getDefinition();
	
	public String describe();
	
	public BuiltinID getBuiltin();
	
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper);
	
	public <T> T accept(MemberVisitor<T> visitor);
	
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor);
	
	public <T> T getTag(Class<T> tag);
	
	public <T> void setTag(Class<T> tag, T value);

	<T> boolean hasTag(Class<T> tag);
	
	DefinitionMemberRef getOverrides();

	public void normalize(TypeScope scope);
	
	boolean isAbstract();
	
	DefinitionMemberRef ref(StoredType type, GenericMapper mapper);
	
	FunctionHeader getHeader();
	
	default AccessScope getAccessScope() {
		return getDefinition().getAccessScope();
	}
}
