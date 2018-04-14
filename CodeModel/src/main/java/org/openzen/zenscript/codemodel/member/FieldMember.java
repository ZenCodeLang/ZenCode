/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class FieldMember extends DefinitionMember {
	public final String name;
	public final ITypeID type;
	public final boolean isFinal;
	public Expression initializer;
	
	public FieldMember(CodePosition position, int modifiers, String name, ITypeID type, boolean isFinal) {
		super(position, modifiers);
		
		this.name = name;
		this.type = type;
		this.isFinal = isFinal;
	}
	
	public void setInitializer(Expression initializer) {
		this.initializer = initializer;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addField(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new FieldMember(position, modifiers, name, type.withGenericArguments(registry, mapping), isFinal);
	}

	@Override
	public String describe() {
		return "field " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitField(this);
	}
}
