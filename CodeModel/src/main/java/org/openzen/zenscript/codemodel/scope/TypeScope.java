/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TypeScope {
	public LocalMemberCache getMemberCache();
	
	default TypeMembers getTypeMembers(ITypeID type) {
		return getMemberCache().get(type);
	}
	
	default GlobalTypeRegistry getTypeRegistry() {
		return getMemberCache().getRegistry();
	}
	
	public AnnotationDefinition getAnnotation(String name);
}
