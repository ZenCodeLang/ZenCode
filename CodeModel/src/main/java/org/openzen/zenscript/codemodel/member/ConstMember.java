/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ref.ConstMemberRef;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstMember extends DefinitionMember {
	public final String name;
	public ITypeID type;
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
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addConst(new ConstMemberRef(this, mapper.map(type)));
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitConst(this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}
}
