/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TypeParameterBound {
	boolean isObjectType();
	
	<T> T accept(GenericParameterBoundVisitor<T> visitor);
	
	<C, R> R accept(C context, GenericParameterBoundVisitorWithContext<C, R> visitor);
	
	void registerMembers(LocalMemberCache cache, TypeMembers type);
	
	boolean matches(LocalMemberCache cache, TypeID type);
	
	TypeParameterBound instance(GenericMapper mapper);
	
	String getCanonical();
}
