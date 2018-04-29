/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
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
public class EnumConstantMember extends DefinitionMember {
	public final String name;
	public final int value;
	
	public NewExpression constructor;
	
	public EnumConstantMember(CodePosition position, String name, int value) {
		super(position, Modifiers.MODIFIER_STATIC);
		
		this.name = name;
		this.value = value;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addEnumMember(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		throw new UnsupportedOperationException("Enums can't have type parameters");
	}

	@Override
	public String describe() {
		return "enum member " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitEnumConstant(this);
	}
}
