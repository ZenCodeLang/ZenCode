/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.HashMap;
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
// How it works:
// - A type definition with type parameters contains an inner definition (which may contain references to one or more type parameters)
// - The type is instanced somewhere
// - The members of that type are instanced and registered to the TypeMembers. This will generate an InstancedInnerDefinitionMember for the inner definition.
public class InstancedInnerDefinitionMember extends DefinitionMember {
	public final HighLevelDefinition definition;
	public final Map<TypeParameter, ITypeID> outerMapping;
	
	public InstancedInnerDefinitionMember(CodePosition position, int modifiers, HighLevelDefinition definition, Map<TypeParameter, ITypeID> outerMapping) {
		super(position, modifiers);
		this.definition = definition;
		this.outerMapping = outerMapping;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addInnerType(this.definition.name, new InnerDefinition(this.definition, outerMapping));
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		if (this.isStatic()) {
			return this;
		} else {
			Map<TypeParameter, ITypeID> totalMap = new HashMap<>();
			for (Map.Entry<TypeParameter, ITypeID> entry : outerMapping.entrySet())
				totalMap.put(entry.getKey(), entry.getValue().withGenericArguments(registry, mapping));
			for (Map.Entry<TypeParameter, ITypeID> entry : mapping.entrySet())
				totalMap.put(entry.getKey(), entry.getValue());
			
			return new InstancedInnerDefinitionMember(position, modifiers, definition, totalMap);
		}
	}

	@Override
	public String describe() {
		return "inner type " + definition.name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Should never be called on this member!");
	}
}
