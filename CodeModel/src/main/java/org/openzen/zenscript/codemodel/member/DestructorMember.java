/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class DestructorMember extends FunctionalMember {
	private static final FunctionHeader HEADER = new FunctionHeader(BasicTypeID.VOID);
	
	public DestructorMember(CodePosition position, HighLevelDefinition definition, int modifiers) {
		super(position, definition, modifiers, "~this", HEADER, null);
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":destructor";
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.DESTRUCTOR;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		if (priority == TypeMemberPriority.SPECIFIED)
			type.addDestructor(ref(mapper), priority);
	}

	@Override
	public String describe() {
		return "destructor";
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitDestructor(this);
	}
}
