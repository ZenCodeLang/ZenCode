/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
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
public class ConstMember extends DefinitionMember {
	public final String name;
	public final ITypeID type;
	public Expression value;
	public final BuiltinID builtin;
	
	public ConstMember(CodePosition position, HighLevelDefinition definition, int modifiers, String name, ITypeID type, BuiltinID builtin) {
		super(position, definition, modifiers);
		
		this.name = name;
		this.type = type;
		this.builtin = builtin;
	}

	@Override
	public String describe() {
		return "const " + name;
	}

	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addConst(this);
	}

	@Override
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return this;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitConst(this);
	}
}
