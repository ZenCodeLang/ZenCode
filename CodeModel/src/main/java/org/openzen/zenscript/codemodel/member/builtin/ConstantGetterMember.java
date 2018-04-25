/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.builtin;

import java.util.Map;
import java.util.function.Function;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.IGettableMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
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
public class ConstantGetterMember extends Taggable implements IGettableMember {
	private final String name;
	private final Function<CodePosition, Expression> value;
	private final ITypeID type;
	
	public ConstantGetterMember(String name, Function<CodePosition, Expression> value) {
		this.name = name;
		this.value = value;
		this.type = value.apply(CodePosition.BUILTIN).type;
	}
	
	@Override
	public CodePosition getPosition() {
		return CodePosition.BUILTIN;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public ITypeID getType() {
		return type;
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public Expression get(CodePosition position, Expression target) {
		throw new CompileException(position, CompileExceptionCode.USING_STATIC_ON_INSTANCE, "Not an instance member");
	}
	
	@Override
	public Expression getStatic(CodePosition position) {
		return value.apply(position);
	}

	@Override
	public String describe() {
		return "constant getter " + name;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addGetter(this, priority);
	}

	@Override
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return this; // not instancable
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not a compilable member");
	}
}
