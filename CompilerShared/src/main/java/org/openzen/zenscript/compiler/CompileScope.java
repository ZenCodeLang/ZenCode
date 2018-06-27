/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import java.util.List;
import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class CompileScope implements TypeScope {
	private final GlobalTypeRegistry globalRegistry;
	private final List<ExpansionDefinition> expansions;
	private final LocalMemberCache cache;
	
	public CompileScope(AccessScope access, GlobalTypeRegistry globalRegistry, List<ExpansionDefinition> expansions) {
		this.globalRegistry = globalRegistry;
		this.expansions = expansions;
		this.cache = new LocalMemberCache(access, globalRegistry, expansions);
	}

	@Override
	public GlobalTypeRegistry getTypeRegistry() {
		return globalRegistry;
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return cache;
	}

	@Override
	public TypeMembers getTypeMembers(ITypeID type) {
		return cache.get(type);
	}
}
