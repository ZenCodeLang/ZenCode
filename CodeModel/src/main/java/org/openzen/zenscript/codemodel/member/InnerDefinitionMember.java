/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class InnerDefinitionMember extends DefinitionMember {
	public final HighLevelDefinition innerDefinition;
	
	public InnerDefinitionMember(CodePosition position, HighLevelDefinition outer, int modifiers, HighLevelDefinition definition) {
		super(position, outer, modifiers);
		
		this.innerDefinition = definition;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addInnerType(innerDefinition.name, new InnerDefinition(innerDefinition));
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		if (this.isStatic()) {
			return this;
		} else {
			return new InstancedInnerDefinitionMember(position, definition, modifiers, innerDefinition, mapping);
		}
	}

	@Override
	public String describe() {
		return "inner type " + innerDefinition.name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitInnerDefinition(this);
	}
}
