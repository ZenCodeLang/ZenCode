package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalMemberCache {
	private final GlobalTypeRegistry registry;
	private final Map<TypeID, TypeMembers> types = new HashMap<>();
	private final List<ExpansionDefinition> expansions = new ArrayList<>();

	public LocalMemberCache(
			GlobalTypeRegistry registry,
			List<ExpansionDefinition> expansions) {
		this.registry = registry;
		this.expansions.addAll(expansions);
	}

	public GlobalTypeRegistry getRegistry() {
		return registry;
	}

	public TypeMembers get(TypeID type) {
		type = type.getNormalized();
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
		members.type.accept(null, new TypeMemberBuilder(registry, members, this));
	}

	public List<ExpansionDefinition> getExpansions() {
		return expansions;
	}
}
