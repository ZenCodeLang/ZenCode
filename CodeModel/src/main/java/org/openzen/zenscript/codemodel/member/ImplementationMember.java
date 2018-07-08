/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberRef;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

/**
 *
 * @author Hoofdgebruiker
 */
public class ImplementationMember extends DefinitionMember {
	public final ITypeID type;
	public final List<IDefinitionMember> members = new ArrayList<>();
	
	public ImplementationMember(CodePosition position, HighLevelDefinition definition, int modifiers, ITypeID type) {
		super(position, definition, modifiers);
		
		this.type = type;
	}
	
	public void addMember(IDefinitionMember member) {
		this.members.add(member);
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		ITypeID instancedType = mapper.map(type);
		members.addImplementation(new ImplementationMemberRef(this, instancedType), priority);
		
		TypeMembers interfaceTypeMembers = members.getMemberCache().get(instancedType);
		interfaceTypeMembers.copyMembersTo(position, members, TypeMemberPriority.INTERFACE);
	}

	@Override
	public String describe() {
		return "implementation of " + type.toString();
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitImplementation(this);
	}
}
