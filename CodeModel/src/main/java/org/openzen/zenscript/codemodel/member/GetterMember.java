/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

/**
 *
 * @author Hoofdgebruiker
 */
public class GetterMember extends PropertyMember {
	public final String name;
	private GetterMemberRef overrides;
	public Statement body = null;
	
	public GetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			ITypeID type,
			BuiltinID builtin) {
		super(position, definition, modifiers, type, builtin);
		
		this.name = name;
	}
	
	public void setBody(Statement body) {
		this.body = body;
		
		if (type == BasicTypeID.UNDETERMINED)
			type = body.getReturnType();
	}
	
	@Override
	public boolean isAbstract() {
		return body == null && builtin == null;
	}
	
	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addGetter(new GetterMemberRef(members.type, this, mapper), priority);
	}

	@Override
	public String describe() {
		return "getter " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitGetter(this);
	}
	
	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitGetter(context, this);
	}

	@Override
	public GetterMemberRef getOverrides() {
		return overrides;
	}

	@Override
	public void normalize(TypeScope scope) {
		if (body != null)
			body = body.normalize(scope, ConcatMap.empty(LoopStatement.class, LoopStatement.class));
	}
	
	public void setOverrides(GetterMemberRef override) {
		this.overrides = override;
		
		if (type == BasicTypeID.UNDETERMINED)
			type = override.getType();
	}

	@Override
	public DefinitionMemberRef ref(ITypeID type, GenericMapper mapper) {
		return new GetterMemberRef(type, this, mapper);
	}
	
	@Override
	public FunctionHeader getHeader() {
		return new FunctionHeader(type);
	}
}
