/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class LocalMemberCache {
	private final AccessScope access;
	private final GlobalTypeRegistry registry;
	private final Map<ITypeID, TypeMembers> types = new HashMap<>();
	private final List<ExpansionDefinition> expansions = new ArrayList<>();
	
	public LocalMemberCache(
			AccessScope access,
			GlobalTypeRegistry registry,
			List<ExpansionDefinition> expansions) {
		this.access = access;
		this.registry = registry;
		this.expansions.addAll(expansions);
	}
	
	public GlobalTypeRegistry getRegistry() {
		return registry;
	}
	
	public TypeMembers get(ITypeID type) {
		if (types.containsKey(type)) {
			return types.get(type);
		} else {
			TypeMembers members = new TypeMembers(this, type);
			registerMembers(members);
			types.put(type, members);
			return members;
		}
	}
	
	private void registerMembers(TypeMembers members) {
		members.type.accept(new TypeMemberBuilder(registry, members, this));
		
		for (ExpansionDefinition expansion : expansions) {
			if (expansion.target == null)
				throw new CompileException(expansion.position, CompileExceptionCode.INTERNAL_ERROR, "Missing expansion target");
			
			Map<TypeParameter, ITypeID> mapping = matchType(members.type, expansion.target);
			if (mapping != null) {
				if (mapping.isEmpty()) {
					for (IDefinitionMember member : expansion.members)
						member.registerTo(members, TypeMemberPriority.SPECIFIED);
				} else {
					for (IDefinitionMember member : expansion.members)
						member.instance(registry, mapping).registerTo(members, TypeMemberPriority.SPECIFIED);
				}
			}
		}
	}
	
	private Map<TypeParameter, ITypeID> matchType(ITypeID type, ITypeID pattern) {
		if (type == pattern)
			return Collections.emptyMap();
		
		Map<TypeParameter, ITypeID> mapping = new HashMap<>();
		if (pattern.inferTypeParameters(this, type, mapping))
			return mapping;
		
		return null;
	}
}
