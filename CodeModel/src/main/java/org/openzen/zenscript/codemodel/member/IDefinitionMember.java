/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDefinitionMember {
	CodePosition getPosition();
	
	int getSpecifiedModifiers();
	
	int getEffectiveModifiers();
	
	MemberAnnotation[] getAnnotations();
	
	HighLevelDefinition getDefinition();
	
	String describe();
	
	BuiltinID getBuiltin();
	
	void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper);
	
	<T> T accept(MemberVisitor<T> visitor);
	
	<C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor);
	
	<T extends Tag> T getTag(Class<T> tag);
	
	<T extends Tag> void setTag(Class<T> tag, T value);

	<T extends Tag> boolean hasTag(Class<T> tag);
	
	DefinitionMemberRef getOverrides();

	void normalize(TypeScope scope);
	
	boolean isAbstract();
	
	DefinitionMemberRef ref(TypeID type, GenericMapper mapper);
	
	FunctionHeader getHeader();
	
	default AccessScope getAccessScope() {
		return getDefinition().getAccessScope();
	}
}
