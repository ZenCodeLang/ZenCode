/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

import java.util.Map;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParameterSuperBound extends GenericParameterBound {
	public final ITypeID type;
	
	public ParameterSuperBound(ITypeID type) {
		this.type = type;
	}

	@Override
	public void registerMembers(LocalMemberCache cache, TypeMembers type) {
		// TODO: nothing?
	}

	@Override
	public boolean matches(ITypeID type) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public GenericParameterBound withGenericArguments(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> arguments) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public <T> T accept(GenericParameterBoundVisitor<T> visitor) {
		return visitor.visitSuper(this);
	}
}
