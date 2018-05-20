/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;

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
	}
	
	public List<ExpansionDefinition> getExpansions() {
		return expansions;
	}
}
