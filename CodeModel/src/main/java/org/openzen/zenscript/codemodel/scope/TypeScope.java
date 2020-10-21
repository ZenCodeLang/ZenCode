/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TypeScope extends TypeResolutionContext {
	public LocalMemberCache getMemberCache();
	
	default TypeMembers getTypeMembers(TypeID type) {
		return getMemberCache().get(type);
	}
	
	@Override
	default GlobalTypeRegistry getTypeRegistry() {
		return getMemberCache().getRegistry();
	}
	
	TypeMemberPreparer getPreparer();
	
	GenericMapper getLocalTypeParameters();
}
