/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public class StaticInitializerMember extends Taggable implements IDefinitionMember {
	private final CodePosition position;
	public Statement body;
	
	public StaticInitializerMember(CodePosition position) {
		this.position = position;
	}
	
	@Override
	public CodePosition getPosition() {
		return position;
	}

	@Override
	public String describe() {
		return "static initializer";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		
	}

	@Override
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return this;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitStaticInitializer(this);
	}
}
