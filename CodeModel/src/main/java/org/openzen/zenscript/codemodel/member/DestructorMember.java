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
public class DestructorMember extends FunctionalMember {
	private static final FunctionHeader HEADER = new FunctionHeader(BasicTypeID.VOID);
	
	public DestructorMember(CodePosition position, HighLevelDefinition definition, int modifiers) {
		super(position, definition, modifiers, "~this", HEADER, null);
	}
	
	@Override
	public String getInformalName() {
		return "destructor";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		if (priority == TypeMemberPriority.SPECIFIED)
			type.addDestructor(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return this;
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
