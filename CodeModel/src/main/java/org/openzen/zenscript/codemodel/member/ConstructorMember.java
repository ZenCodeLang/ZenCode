/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstructorMember extends FunctionalMember {
	public ConstructorMember(CodePosition position, int modifiers, FunctionHeader header) {
		super(position, modifiers, new FunctionHeader(header.typeParameters, BasicTypeID.VOID, header.parameters));
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		if (priority == TypeMemberPriority.SPECIFIED)
			type.addConstructor(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new ConstructorMember(position, modifiers, header.instance(registry, mapping));
	}

	@Override
	public String describe() {
		return "constructor " + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitConstructor(this);
	}
}
