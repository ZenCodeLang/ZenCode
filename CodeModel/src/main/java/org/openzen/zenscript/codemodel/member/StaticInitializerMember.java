/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public class StaticInitializerMember extends Taggable implements IDefinitionMember {
	public final CodePosition position;
	public Statement body;
	public MemberAnnotation[] annotations = MemberAnnotation.NONE;
	
	public StaticInitializerMember(CodePosition position) {
		this.position = position;
	}
	
	@Override
	public CodePosition getPosition() {
		return position;
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public String describe() {
		return "static initializer";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitStaticInitializer(this);
	}
}
