/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.SetterMemberRef;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class SetterMember extends FunctionalMember {
	public final ITypeID type;
	
	public SetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			ITypeID type,
			BuiltinID builtin)
	{
		super(position,
				definition,
				modifiers,
				name,
				new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(type, "$")),
				builtin);
		
		this.type = type;
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":setter:" + name;
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.SETTER;
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addSetter(new SetterMemberRef(this, mapper.map(type)), priority);
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
