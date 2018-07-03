/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class MethodMember extends FunctionalMember {
	public MethodMember(CodePosition position, HighLevelDefinition definition, int modifiers, String name, FunctionHeader header, BuiltinID builtin) {
		super(position, definition, modifiers, name, header, builtin);
	}
	
	@Override
	public String getInformalName() {
		return name + " method";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addMethod(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new MethodMember(
				position,
				definition,
				modifiers,
				name,
				header.instance(registry, mapping),
				builtin);
	}

	@Override
	public String describe() {
		return name + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitMethod(this);
	}
}
