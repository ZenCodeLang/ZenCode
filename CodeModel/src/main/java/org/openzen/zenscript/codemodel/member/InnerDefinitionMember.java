/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class InnerDefinitionMember extends DefinitionMember {
	public final HighLevelDefinition innerDefinition;
	
	public InnerDefinitionMember(CodePosition position, HighLevelDefinition outer, int modifiers, HighLevelDefinition definition) {
		super(position, outer, definition instanceof InterfaceDefinition ? modifiers | Modifiers.STATIC : modifiers);
		
		this.innerDefinition = definition;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		if (isStatic() || mapper.getMapping().isEmpty()) {
			type.addInnerType(innerDefinition.name, new InnerDefinition(innerDefinition));
		} else {
			type.addInnerType(innerDefinition.name, new InnerDefinition(innerDefinition, mapper.getMapping()));
		}
	}

	@Override
	public String describe() {
		return "inner type " + innerDefinition.name;
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitInnerDefinition(this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public void normalize(TypeScope scope) {
		innerDefinition.normalize(scope);
	}
}
