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
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class FunctionalMember extends DefinitionMember {
	public FunctionHeader header;
	public final BuiltinID builtin;
	public Statement body = null;
	
	public FunctionalMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			FunctionHeader header,
			BuiltinID builtin) {
		super(position, definition, modifiers);
		
		this.header = header;
		this.builtin = builtin;
	}
	
	public void setBody(Statement body) {
		this.body = body;
	}
	
	public abstract String getCanonicalName();
	
	public abstract FunctionalKind getKind();
	
	public FunctionalMemberRef ref(ITypeID type, GenericMapper mapper) {
		return new FunctionalMemberRef(this, type, mapper);
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}

	@Override
	public void normalize(TypeScope scope) {
		header = header.normalize(scope.getTypeRegistry());
		if (body != null)
			body = body.normalize(scope, ConcatMap.empty(LoopStatement.class, LoopStatement.class));
	}
	
	@Override
	public boolean isAbstract() {
		return body == null && builtin == null;
	}
	
	@Override
	public FunctionHeader getHeader() {
		return header;
	}
}
