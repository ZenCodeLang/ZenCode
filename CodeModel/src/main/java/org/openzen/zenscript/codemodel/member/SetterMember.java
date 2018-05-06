/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
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
public class SetterMember extends FunctionalMember {
	public final String name;
	public final ITypeID type;
	
	public SetterMember(CodePosition position, HighLevelDefinition definition, int modifiers, String name, ITypeID type) {
		super(position, definition, modifiers, name, new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(type, "$")));
		
		this.name = name;
		this.type = type;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addSetter(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new SetterMember(position, definition, modifiers, name, type.withGenericArguments(registry, mapping));
	}

	@Override
	public String describe() {
		return "setter " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitSetter(this);
	}
}
