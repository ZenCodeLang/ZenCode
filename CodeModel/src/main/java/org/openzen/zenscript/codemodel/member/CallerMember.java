/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CallerMember extends FunctionalMember {
	public CallerMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			FunctionHeader header,
			BuiltinID builtin)
	{
		super(position, definition, modifiers, "()", header, builtin);
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":caller:" + header.getCanonical();
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.CALLER;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addCaller(ref(mapper), priority);
	}

	@Override
	public String describe() {
		return "caller " + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitCaller(this);
	}
}
