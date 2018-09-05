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
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class MethodMember extends FunctionalMember {
	public final String name;
	private FunctionalMemberRef overrides;
	
	public MethodMember(CodePosition position, HighLevelDefinition definition, int modifiers, String name, FunctionHeader header, BuiltinID builtin) {
		super(position, definition, modifiers, header, builtin);
		
		this.name = name;
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":" + name + header.getCanonical();
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.METHOD;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addMethod(name, ref(type.type, mapper), priority);
	}

	@Override
	public String describe() {
		return name + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitMethod(this);
	}
	
	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitMethod(context, this);
	}

	@Override
	public FunctionalMemberRef getOverrides() {
		return overrides;
	}
	
	public void setOverrides(GlobalTypeRegistry registry, FunctionalMemberRef overrides) {
		this.overrides = overrides;
		header = header.inferFromOverride(registry, overrides.getHeader());
		
		if (overrides.getTarget().isPublic())
			modifiers |= Modifiers.PUBLIC;
		if (overrides.getTarget().isProtected())
			modifiers |= Modifiers.PROTECTED;
	}
}
