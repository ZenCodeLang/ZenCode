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
	private final HighLevelDefinition definition;
	
	public InnerDefinitionMember(CodePosition position, int modifiers, HighLevelDefinition definition) {
		super(position, modifiers);
		
		this.definition = definition;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addInnerType(definition.name, new InnerDefinition(definition));
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		if (this.isStatic()) {
			return this;
		} else {
			return new InstancedInnerDefinitionMember(position, modifiers, definition, mapping);
		}
	}

	@Override
	public String describe() {
		return "inner type " + definition.name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitInnerDefinition(this);
	}
}
