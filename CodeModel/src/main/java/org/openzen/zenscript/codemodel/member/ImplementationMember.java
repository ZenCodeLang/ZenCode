/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.shared.CodePosition;

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
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addImplementation(this, priority);
		
		TypeMembers interfaceTypeMembers = type.getMemberCache().get(this.type);
		interfaceTypeMembers.copyMembersTo(position, type, TypeMemberPriority.INTERFACE);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		ITypeID instancedType = type.withGenericArguments(registry, mapping);
		ImplementationMember result = new ImplementationMember(position, definition, modifiers, instancedType);
		for (IDefinitionMember member : members)
			result.addMember(member.instance(registry, mapping));
		return result;
	}

	@Override
	public String describe() {
		return "implementation " + type.toString();
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
