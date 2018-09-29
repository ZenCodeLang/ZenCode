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
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class StaticInitializerMember extends DefinitionMember {
	public Statement body;
	
	public StaticInitializerMember(CodePosition position, HighLevelDefinition definition) {
		super(position, definition, 0);
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
	
	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitStaticInitializer(context, this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public void normalize(TypeScope scope) {
		body = body.normalize(scope, ConcatMap.empty(LoopStatement.class, LoopStatement.class));
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public DefinitionMemberRef ref(StoredType type, GenericMapper mapper) {
		throw new UnsupportedOperationException("Cannot reference a static initializer");
	}
	
	@Override
	public FunctionHeader getHeader() {
		return new FunctionHeader(BasicTypeID.VOID);
	}
}
