/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IDefinitionMember {
	public CodePosition getPosition();
	
	public String describe();
	
	public void registerTo(TypeMembers type, TypeMemberPriority priority);
	
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping);
	
	public <T> T accept(MemberVisitor<T> visitor);
}
