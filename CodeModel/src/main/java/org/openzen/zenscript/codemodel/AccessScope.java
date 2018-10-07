/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.Objects;

/**
 *
 * @author Hoofdgebruiker
 */
public final class AccessScope {
	public final Module module;
	public final HighLevelDefinition definition;
	
	public AccessScope(Module module, HighLevelDefinition definition) {
		this.module = module;
		this.definition = definition;
	}
	
	public boolean hasAccessTo(AccessScope other, int access) {
		if (Modifiers.isPublic(access))
			return true;
		if (definition == other.definition)
			return true;
		if (Modifiers.isPrivate(access))
			return false;
		if (Modifiers.isInternal(access))
			return module == other.module;
		if (Modifiers.isProtected(access))
			return definition.isSubclassOf(other.definition);
		
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.module);
		hash = 79 * hash + Objects.hashCode(this.definition);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		
		final AccessScope other = (AccessScope) obj;
		return module == other.module && definition == other.definition;
	}
}
