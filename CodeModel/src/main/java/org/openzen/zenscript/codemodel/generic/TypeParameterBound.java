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
public abstract class TypeParameterBound {
	public abstract boolean isObjectType();
	
	public abstract <T> T accept(GenericParameterBoundVisitor<T> visitor);
	
	public abstract <C, R> R accept(C context, GenericParameterBoundVisitorWithContext<C, R> visitor);
	
	public abstract void registerMembers(LocalMemberCache cache, TypeMembers type);
	
	public abstract boolean matches(LocalMemberCache cache, TypeID type);
	
	public abstract TypeParameterBound instance(GenericMapper mapper);
	
	public abstract String getCanonical();
}
