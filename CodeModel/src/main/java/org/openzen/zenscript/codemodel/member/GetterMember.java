/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetterMember extends FunctionalMember {
	public final String name;
	public final ITypeID type;
	
	public GetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			ITypeID type,
			BuiltinID builtin) {
		super(position, definition, modifiers, name, new FunctionHeader(type), builtin);
		
		this.name = name;
		this.type = type;
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":getter:" + name;
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.GETTER;
	}
	
	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addGetter(new GetterMemberRef(this, mapper.map(type)), priority);
	}

	@Override
	public String describe() {
		return "getter " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitGetter(this);
	}
}
