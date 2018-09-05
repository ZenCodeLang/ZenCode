/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.SetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class SetterMember extends PropertyMember {
	public final String name;
	private SetterMemberRef overrides;
	public Statement body;
	public final FunctionParameter parameter;
	
	public SetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			ITypeID type,
			BuiltinID builtin)
	{
		super(position,
				definition,
				modifiers,
				type,
				builtin);
		
		this.name = name;
		this.parameter = new FunctionParameter(type, "value");
	}
	
	public void setBody(Statement body) {
		this.body = body;
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addSetter(new SetterMemberRef(this, mapper), priority);
	}

	@Override
	public String describe() {
		return "setter " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitSetter(this);
	}
	
	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitSetter(context, this);
	}

	@Override
	public SetterMemberRef getOverrides() {
		return overrides;
	}

	@Override
	public void normalize(TypeScope scope) {
		if (body != null)
			body = body.normalize(scope, ConcatMap.empty(LoopStatement.class, LoopStatement.class));
	}
	
	@Override
	public boolean isAbstract() {
		return body == null && builtin == null;
	}
	
	public void setOverrides(SetterMemberRef overrides) {
		this.overrides = overrides;
		
		if (type == BasicTypeID.UNDETERMINED)
			type = overrides.getType();
	}
}
