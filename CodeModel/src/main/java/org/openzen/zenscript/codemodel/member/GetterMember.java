/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetterMember extends FunctionalMember implements IGettableMember {
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
	public String getInformalName() {
		return "getter " + name;
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
	public Expression get(CodePosition position, Expression target) {
		return new GetterExpression(position, target, this);
	}
	
	@Override
	public Expression getStatic(CodePosition position) {
		return new StaticGetterExpression(position, this);
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addGetter(this, priority);
	}

	@Override
	public GetterMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new GetterMember(
				position,
				definition,
				modifiers,
				name,
				type.withGenericArguments(registry, mapping),
				builtin);
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
